package ru.vladislavsumin.feature.logViewer.domain.logs.delegates.filter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import ru.vladislavsumin.core.utils.measureTimeMillisWithResult
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.ClearLogState
import ru.vladislavsumin.feature.logViewer.domain.logs.FilterRequest
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo

/**
 * Делегат ответственный за все связанное с фильтрацией логов.
 *
 * @param logs исходный список логов.
 */
internal class LogFilterDelegate(
    private val logs: Flow<ClearLogState>,
) {
    fun createFilterProgressFlow(filter: Flow<FilterRequest>): Flow<FilterLogProgress> = flow {
        // Кеширует последнее успешное отфильтрованное состояние.
        var filteredCache = emptyList<LogRecord>()

        combine(
            logs,
            filter,
        ) { logs, filter -> logs to filter }
            .distinctUntilChanged()
            .transformLatest { (logs, filter) ->
                // Сначала отправляем состояние прогресса и старый результат поиска, для возможности отображать
                // старый фильтр пока идет процесс фильтрации по новому запросу.
                emit(
                    value = FilterLogProgress(
                        isFilteringNow = true,
                        logs = filteredCache,
                        totalLogRecords = logs.logs.size,
                        runIdOrders = logs.runIdIndexes,
                    ),
                )
                filteredCache = filterLogs(logs.logs, filter, logs.runIdIndexes)
                emit(
                    value = FilterLogProgress(
                        isFilteringNow = false,
                        logs = filteredCache,
                        totalLogRecords = logs.logs.size,
                        runIdOrders = logs.runIdIndexes,
                    ),
                )
            }
            .collect(this)
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod") // TODO переписать фильтр
    private fun filterLogs(
        logs: List<LogRecord>,
        filter: FilterRequest,
        runIdOrders: List<RunIdInfo>?,
    ): List<LogRecord> {
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
                .let {
                    if (filter.runOrders.isNotEmpty()) {
                        val orders = filter.runOrders.mapNotNull { index ->
                            runIdOrders?.getOrNull(index)?.orderRange
                        }
                        it.filter { log ->
                            orders.any { index -> log.order in index }
                        }
                    } else {
                        it
                    }
                }
                .filter { log ->
                    filter.filters.all { (field, filter) ->
                        val range: IntRange? = when (field) {
                            FilterRequest.Field.All -> 0..<log.raw.length
                            FilterRequest.Field.Tag -> log.tag
                            FilterRequest.Field.ProcessId -> log.processId
                            FilterRequest.Field.Thread -> log.thread
                            FilterRequest.Field.Message -> log.message
                        }

                        if (range == null) return@all false

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
}
