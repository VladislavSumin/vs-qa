package ru.vladislavsumin.feature.logViewer.domain.logs.delegates.search

import ru.vladislavsumin.core.boyerMooreSearch.toBoyerMoorePattern
import ru.vladislavsumin.core.utils.measureTimeMillisWithResult
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.domain.logs.SearchRequest

internal class LogSearchDelegate {
    @Suppress("LongMethod")
    fun searchLogs(
        logs: List<LogRecord>,
        search: SearchRequest,
        totalLogRecords: Int,
        runIdOrders: List<RunIdInfo>?,
    ): LogIndex {
        val (time, result) = measureTimeMillisWithResult {
            // TODO Убрать дублирование кода.
            val searchedLogs: List<LogRecord> = if (search.useRegex) {
                val regex = runCatching {
                    Regex(
                        pattern = search.search,
                        options = buildSet {
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

                logs.parallelStream().map { log ->
                    val maths = regex.findAll(log.raw)
                    val ranges = maths.map { it.range }.toList()
                    if (ranges.isNotEmpty()) {
                        log.copy(searchHighlights = ranges)
                    } else {
                        log
                    }
                }.toList()
            } else {
                val pattern = search.search.toBoyerMoorePattern(ignoreCase = !search.matchCase)

                logs.parallelStream().map { log ->
                    val maths = pattern.search(log.raw)
                    val ranges = maths.map { IntRange(it, it + search.search.length - 1) }.toList()
                    if (ranges.isNotEmpty()) {
                        log.copy(searchHighlights = ranges)
                    } else {
                        log
                    }
                }.toList()
            }

            val searchIndex = searchedLogs.mapIndexedNotNull { index, record ->
                if (record.searchHighlights != null) index else null
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
