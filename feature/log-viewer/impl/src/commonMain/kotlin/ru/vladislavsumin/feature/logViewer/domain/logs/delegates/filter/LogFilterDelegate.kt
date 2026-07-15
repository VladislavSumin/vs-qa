package ru.vladislavsumin.feature.logViewer.domain.logs.delegates.filter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.ClearLogState
import ru.vladislavsumin.feature.logViewer.domain.logs.FilterRequest
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import kotlin.time.measureTimedValue

/**
 * Делегат ответственный за все связанное с фильтрацией логов.
 *
 * @param logs исходный список логов.
 */
internal class LogFilterDelegate(private val logs: Flow<ClearLogState>) {
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

    private fun filterLogs(
        logs: List<LogRecord>,
        filter: FilterRequest,
        runIdOrders: List<RunIdInfo>?,
    ): List<LogRecord> {
        val (result, time) = measureTimedValue {
            val prepared = filter.operation.prepare(runIdOrders) ?: return logs
            logs.parallelStream()
                .filter { log -> prepared.check(log) }
                .toList()
        }
        LogLogger.d { "Log filtered at $time, size = ${result.size}" }
        return result
    }
}
