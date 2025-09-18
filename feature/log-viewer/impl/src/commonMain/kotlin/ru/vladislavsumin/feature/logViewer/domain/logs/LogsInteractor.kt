package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.coroutines.utils.mapState
import ru.vladislavsumin.core.utils.measureTimeMillisWithResult
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractor
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractorImpl
import java.nio.file.Path
import kotlin.math.log

/**
 * **Внимание, данный interactor является stateful.**
 */
interface LogsInteractor {
    /**
     * Возвращает текущий статус загрузки логов.
     */
    fun observeLoadingStatus(): StateFlow<LoadingStatus>

    fun observeMappingStatus(): StateFlow<MappingStatus>

    suspend fun detachMapping()
    suspend fun attachMapping(path: Path)

    /**
     * Строит "Индекс" (результат фильтрации и последующего поиска) на основе [filter] и [search].
     */
    fun observeLogIndex(
        filter: Flow<FilterRequest>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress>

    /**
     * Статус загрузки логов.
     *
     * - [LoadingLogs] логи загружаются
     * - [DeobfuscateLogs] obfuscated логи загружены и с ними уже можно работать, происходит преобразование логов.
     * - [Loaded] логи полностью загружены и обработаны.
     */
    sealed interface LoadingStatus {
        data object LoadingLogs : LoadingStatus
        data object DeobfuscateLogs : LoadingStatus
        data object Loaded : LoadingStatus
    }

    sealed interface MappingStatus {
        data object NotAttached : MappingStatus
        data object Attached : MappingStatus
    }
}

// TODO тут нужно оптимизировать количество копирований списка, а так же equals проверки.
class LogsInteractorImpl(
    private val scope: CoroutineScope,
    private val logPath: Path,
    private val logParserProvider: LogParserProvider,
    proguardInteractor: ProguardInteractor?,
) : LogsInteractor {
    private val logs = MutableStateFlow<List<RawLogRecord>>(emptyList())
    private val loadingStatus = MutableStateFlow<LogsInteractor.LoadingStatus>(LogsInteractor.LoadingStatus.LoadingLogs)
    private val proguard = MutableStateFlow(proguardInteractor)

    init {
        loadLogs()
    }

    private fun loadLogs() {
        scope.launch(Dispatchers.IO) {
            proguard.collectLatest { proguard ->
                loadingStatus.value = LogsInteractor.LoadingStatus.LoadingLogs
                logs.value = emptyList()

                val obfuscatedLogs = logParserProvider.getLogParser().parseLog(logPath)
                logs.value = obfuscatedLogs

                // TODO ну парсим тут чего уж там, все равно говнокод
                if (proguard != null) {
                    loadingStatus.value = LogsInteractor.LoadingStatus.DeobfuscateLogs
                    val deobfuscated = obfuscatedLogs.parallelStream()
                        .map { log ->
                            val deobfuscatedTag = proguard.deobfuscateClass(log.raw.substring(log.tag))
                            if (deobfuscatedTag != null) {
                                log.copyTag(deobfuscatedTag)
                            } else {
                                log
                            }
                        }
                        .map { log ->
                            if (log.lines > 2 && log.raw.lines()[log.lines - 2].startsWith("\tat ")) {
                                val newMessage = proguard.deobfuscateStack(log.raw.substring(log.message))
                                log.copy(
                                    raw = log.raw.replaceRange(log.message, newMessage),
                                    message = IntRange(log.message.first, log.message.first + newMessage.length - 1),
                                )
                            } else {
                                log
                            }
                        }
                        .toList()
                    logs.value = deobfuscated
                }
                loadingStatus.value = LogsInteractor.LoadingStatus.Loaded
            }
        }
    }

    override fun observeMappingStatus(): StateFlow<LogsInteractor.MappingStatus> = proguard.mapState {
        if (it == null) {
            LogsInteractor.MappingStatus.NotAttached
        } else {
            LogsInteractor.MappingStatus.Attached
        }
    }

    override suspend fun detachMapping() {
        proguard.value = null
    }

    override suspend fun attachMapping(path: Path) {
        proguard.value = ProguardInteractorImpl(path)
    }

    override fun observeLoadingStatus(): StateFlow<LogsInteractor.LoadingStatus> = loadingStatus

    override fun observeLogIndex(
        filter: Flow<FilterRequest>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress> = channelFlow {
        // Если этот кеш не null, то в нем содержаться актуальные или прошлые результаты поиска
        var searchCache: LogIndex?

        createFilterProgressFlow(filter).collectLatest { filterProgress ->
            // При изменении данных поиска всегда обнуляем кеш.
            searchCache = null

            val logs = filterProgress.logs.toLogRecords()
            search.collectLatest { search ->
                // Сразу отправляем результаты поиска + старый кеш далее
                val isSearch = search.search.isNotEmpty()
                if (!isSearch) {
                    searchCache = null
                }
                send(
                    element = LogIndexProgress(
                        isFilteringNow = filterProgress.isFilteringNow,
                        isSearchingNow = isSearch,
                        lastSuccessIndex = searchCache ?: LogIndex(
                            logs = logs,
                            searchIndex = LogIndex.SearchIndex.NoSearch,
                            totalLogRecords = filterProgress.totalLogRecords,
                        ),
                    ),
                )

                // Проводим новый поиск
                if (!filterProgress.isFilteringNow && search.search.isNotEmpty()) {
                    val logIndex = logs.searchLogs(search, filterProgress.totalLogRecords)
                    searchCache = logIndex
                    send(
                        element = LogIndexProgress(
                            isFilteringNow = false,
                            isSearchingNow = false,
                            lastSuccessIndex = logIndex,
                        ),
                    )
                }
            }
        }
    }.flowOn(Dispatchers.Default)

    private fun createFilterProgressFlow(filter: Flow<FilterRequest>): Flow<FilterLogProgress> = flow {
        var filteredCache = emptyList<RawLogRecord>()
        combine(
            logs,
            filter,
        ) { logs, filter -> logs to filter }
            .distinctUntilChanged()
            .transformLatest { (logs, filter) ->
                emit(FilterLogProgress(isFilteringNow = true, filteredCache, logs.size))
                filteredCache = filterLogs(logs, filter)
                emit(FilterLogProgress(isFilteringNow = false, filteredCache, logs.size))
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

    private fun List<LogRecord>.searchLogs(search: SearchRequest, totalLogRecords: Int): LogIndex {
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
                    totalLogRecords = totalLogRecords,
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
                totalLogRecords = totalLogRecords,
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
        val totalLogRecords: Int,
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
    logLevel = logLevel,
)

private fun List<RawLogRecord>.toLogRecords(): List<LogRecord> = map { it.toLogRecord() }
