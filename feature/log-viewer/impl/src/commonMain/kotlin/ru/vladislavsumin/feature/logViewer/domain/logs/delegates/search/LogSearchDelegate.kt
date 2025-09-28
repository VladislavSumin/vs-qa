package ru.vladislavsumin.feature.logViewer.domain.logs.delegates.search

import ru.vladislavsumin.core.utils.measureTimeMillisWithResult
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.domain.logs.SearchRequest

internal class LogSearchDelegate {
    fun searchLogs(
        logs: List<LogRecord>,
        search: SearchRequest,
        totalLogRecords: Int,
        runIdOrders: List<RunIdInfo>?,
    ): LogIndex {
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
                    logs = logs,
                    searchIndex = LogIndex.SearchIndex.BadRegex,
                    totalLogRecords = totalLogRecords,
                    runIdOrders = runIdOrders,
                )
            }

            val searchedLogs = logs.parallelStream().map { log ->
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
                runIdOrders = runIdOrders,
            )
        }

        LogLogger.d {
            "Log searched at ${time}ms, size = ${result.logs.size}, results = ${result.searchIndex.index.size}"
        }

        return result
    }
}
