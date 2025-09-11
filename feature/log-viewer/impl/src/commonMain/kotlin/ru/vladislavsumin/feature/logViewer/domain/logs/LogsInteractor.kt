package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.utils.measureTimeMillisWithResult
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractor
import java.nio.file.Path

interface LogsInteractor {
    fun observeLoadingStatus(): StateFlow<LoadingStatus>
    fun observeLogIndex(
        filter: Flow<FilterRequest>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress>

    sealed interface LoadingStatus {
        data object LoadingLogs : LoadingStatus
        data object Loaded : LoadingStatus
    }
}

// TODO тут нужно оптимизировать количество копирований списка, а так же equals проверки.
class LogsInteractorImpl(
    private val scope: CoroutineScope,
    private val logPath: Path,
    private val proguardInteractor: ProguardInteractor?,
) : LogsInteractor {
    // TODO ну сделать приватным же
    val logs = MutableStateFlow<List<RawLogRecord>>(emptyList())
    private val loadingStatus = MutableStateFlow<LogsInteractor.LoadingStatus>(LogsInteractor.LoadingStatus.LoadingLogs)

    init {
        loadLogs()
    }

    private fun loadLogs() {
        scope.launch(Dispatchers.IO) {
            val initialLogs = AnimeLogParser(proguardInteractor).parseLog(logPath)
            // TODO ну парсим тут чего уж там, все равно говнокод
            val finalLogs = if (proguardInteractor != null) {
                initialLogs.parallelStream()
                    .map { log ->
                        if (log.lines > 2 && log.raw.lines()[log.lines - 2].startsWith("\tat ")) {
                            val newMessage = proguardInteractor.deobfuscateStack(log.raw.substring(log.message))
                            log.copy(
                                raw = log.raw.replaceRange(log.message, newMessage),
                                message = IntRange(log.message.first, log.message.first + newMessage.length - 1),
                            )
                        } else {
                            log
                        }
                    }
                    .toList()
            } else {
                initialLogs
            }
            logs.value = finalLogs
            loadingStatus.value = LogsInteractor.LoadingStatus.Loaded
        }
    }

    override fun observeLoadingStatus(): StateFlow<LogsInteractor.LoadingStatus> = loadingStatus

    override fun observeLogIndex(
        filter: Flow<FilterRequest>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress> {
        return combineTransform(
            createFilterProgressFlow(filter),
            search.distinctUntilChanged(),
        ) { filterProgress, search ->
            val logs = filterProgress.logs.toLogRecords()
            emit(
                value = LogIndexProgress(
                    isFilteringNow = filterProgress.isFilteringNow,
                    isSearchingNow = search.search.isNotEmpty(),
                    lastSuccessIndex = LogIndex(
                        logs = logs,
                        searchIndex = LogIndex.SearchIndex.NoSearch,
                    ),
                ),
            )

            if (!filterProgress.isFilteringNow && search.search.isNotEmpty()) {
                val logIndex = logs.searchLogs(search)
                emit(
                    value = LogIndexProgress(
                        isFilteringNow = false,
                        isSearchingNow = false,
                        lastSuccessIndex = logIndex,
                    ),
                )
            }
        }
            .flowOn(Dispatchers.Default)
    }

    private fun createFilterProgressFlow(filter: Flow<FilterRequest>): Flow<FilterLogProgress> = flow {
        var filteredCache = emptyList<RawLogRecord>()
        combine(
            logs,
            filter,
        ) { logs, filter -> logs to filter }
            .distinctUntilChanged()
            .transformLatest { (logs, filter) ->
                emit(FilterLogProgress(isFilteringNow = true, filteredCache))
                filteredCache = filterLogs(logs, filter)
                emit(FilterLogProgress(isFilteringNow = false, filteredCache))
            }
            .collect(this)
    }

    private fun filterLogs(logs: List<RawLogRecord>, filter: FilterRequest): List<RawLogRecord> {
        val (time, result) = measureTimeMillisWithResult {
            logs.parallelStream()
                .let {
                    if (filter.timeAfter != null) {
                        it.filter { log ->
                            val time = log.raw.substring(log.time)
                            time >= filter.timeAfter
                        }
                    } else {
                        it
                    }
                }
                .let {
                    if (filter.timeBefore != null) {
                        it.filter { log ->
                            val time = log.raw.substring(log.time)
                            time <= filter.timeBefore
                        }
                    } else {
                        it
                    }
                }
                .let {
                    if (filter.minLevel != null) {
                        it.filter { log ->
                            log.logLevel.rawLevel >= filter.minLevel.rawLevel
                        }
                    } else {
                        it
                    }
                }
                .filter { log ->
                    filter.filters.all { (field, filter) ->
                        val range: IntRange = when (field) {
                            FilterRequest.Field.All -> 0..<log.raw.length
                            FilterRequest.Field.Tag -> log.tag
                            FilterRequest.Field.Thread -> log.thread
                            FilterRequest.Field.Message -> log.message
                        }

                        filter.any { operation ->
                            when (operation) {
                                is FilterRequest.Operation.Contains -> log.raw.substring(range)
                                    .contains(operation.data, ignoreCase = true)

                                is FilterRequest.Operation.Exactly -> log.raw.substring(range)
                                    .equals(operation.data, ignoreCase = true)
                            }
                        }
                    }
                }
                .toList()
        }
        LogLogger.d { "Log filtered at ${time}ms, size = ${result.size}" }
        return result
    }

    private fun List<LogRecord>.searchLogs(search: SearchRequest): LogIndex {
        val (time, result) = measureTimeMillisWithResult {
            val regex = runCatching {
                Regex(
                    pattern = search.search,
                    options = buildSet {
                        if (!search.useRegex) {
                            add(RegexOption.LITERAL)
                        }
                        if (!search.matchCase) {
                            add(RegexOption.IGNORE_CASE)
                        }
                    },
                )
            }.getOrElse {
                return@measureTimeMillisWithResult LogIndex(
                    logs = this,
                    searchIndex = LogIndex.SearchIndex.BadRegex,
                )
            }

            val searchedLogs = this.parallelStream().map { log ->
                val math = regex.find(log.raw)
                val range = math?.range
                range?.let { log.copy(searchHighlight = it) } ?: log
            }.toList()

            val searchIndex = searchedLogs.mapIndexedNotNull { index, record ->
                if (record.searchHighlight != null) index else null
            }

            LogIndex(
                logs = searchedLogs,
                searchIndex = if (searchIndex.isNotEmpty()) {
                    LogIndex.SearchIndex.Search(
                        searchIndex,
                    )
                } else {
                    LogIndex.SearchIndex.EmptySearch
                },
            )
        }

        LogLogger.d {
            "Log searched at ${time}ms, size = ${result.logs.size}, results = ${result.searchIndex.index.size}"
        }

        return result
    }

    private data class FilterLogProgress(
        val isFilteringNow: Boolean,
        val logs: List<RawLogRecord>,
    )
}

private fun RawLogRecord.toLogRecord() = LogRecord(
    order = order,
    raw = raw,
    time = time,
    timeInstant = timeInstant,
    level = level,
    thread = thread,
    tag = tag,
    message = message,
    searchHighlight = null,
)

private fun List<RawLogRecord>.toLogRecords(): List<LogRecord> = map { it.toLogRecord() }
