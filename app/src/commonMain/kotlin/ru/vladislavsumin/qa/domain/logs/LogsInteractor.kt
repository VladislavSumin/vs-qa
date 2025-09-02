package ru.vladislavsumin.qa.domain.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import ru.vladislavsumin.qa.LogLogger
import ru.vladislavsumin.qa.utils.measureTimeMillisWithResult
import java.nio.file.Path

interface LogsInteractor {
    fun observeLogIndex(
        filter: Flow<String>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress>
}

// TODO тут нужно оптимизировать количество копирований списка, а так же equals проверки.
class LogsInteractorImpl(
    private val logPath: Path,
) : LogsInteractor {
    val logs = loadLogs()

    fun loadLogs(): List<RawLogRecord> {
        return AnimeLogParser().parseLog(logPath)
    }

    override fun observeLogIndex(
        filter: Flow<String>,
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
    }

    private fun createFilterProgressFlow(filter: Flow<String>): Flow<FilterLogProgress> = flow {
        var filteredCache = emptyList<RawLogRecord>()
        filter
            .distinctUntilChanged()
            .transformLatest { filter ->
                emit(FilterLogProgress(isFilteringNow = true, filteredCache))
                filteredCache = filterLogs { it.raw.contains(filter) }
                emit(FilterLogProgress(isFilteringNow = false, filteredCache))
            }
            .collect(this)
    }

    private fun filterLogs(filter: (RawLogRecord) -> Boolean): List<RawLogRecord> {
        val (time, result) = measureTimeMillisWithResult {
            logs.parallelStream()
                .filter(filter)
                .toList()
        }
        LogLogger.d { "Log filtered at ${time}ms, size = ${result.size}" }
        return result
    }

    private fun List<LogRecord>.searchLogs(search: SearchRequest): LogIndex {
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
            return LogIndex(
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

        return LogIndex(
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

    private data class FilterLogProgress(
        val isFilteringNow: Boolean,
        val logs: List<RawLogRecord>,
    )
}

private fun RawLogRecord.toLogRecord() = LogRecord(
    order = order,
    raw = raw,
    time = time,
    level = level,
    thread = thread,
    tag = tag,
    message = message,
    searchHighlight = null,
)

private fun List<RawLogRecord>.toLogRecords(): List<LogRecord> = map { it.toLogRecord() }
