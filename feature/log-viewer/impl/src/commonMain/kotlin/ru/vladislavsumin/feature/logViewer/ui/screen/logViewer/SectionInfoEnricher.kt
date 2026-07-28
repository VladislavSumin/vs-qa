package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndexProgress
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsViewState
import kotlin.time.measureTimedValue

internal class SectionInfoEnricher {
    fun enrich(searchIndex: Flow<LogIndexProgress>): Flow<EnrichResult> {
        // Особенности LogIndexProgress у него может быть несколько эмитов с одним lastSuccessIndex,
        // поэтому кешируем последний результат и если потом lastSuccessIndex равны то возвращаем кешированное значение.
        var logIndex: LogIndex? = null
        var cache: List<LogsViewState.SectionInfo>? = null

        return searchIndex.map { index ->
            val sectionInfos = if (logIndex == index.lastSuccessIndex && cache != null) {
                LogViewerLogger.t { "SectionInfoEnricher#enrich(): skip, use cache" }
                cache!!
            } else {
                groupLogsByRun(
                    index.lastSuccessIndex.logs,
                    index.lastSuccessIndex.runIdOrders,
                ).also {
                    cache = it
                    logIndex = index.lastSuccessIndex
                }
            }

            EnrichResult(
                searchIndexProgress = index,
                sectionInfos = sectionInfos,
            )
        }
    }

    private fun groupLogsByRun(logs: List<LogRecord>, runIdOrders: List<RunIdInfo>?): List<LogsViewState.SectionInfo> {
        val (result, time) = measureTimedValue {
            if (runIdOrders == null) {
                listOf(LogsViewState.SectionInfo(logs = logs, meta = null))
            } else {
                var logIndex = 0
                runIdOrders.map { info ->
                    val startIndex = logIndex
                    while (logIndex < logs.size && logs[logIndex].order <= info.orderRange.last) {
                        logIndex++
                    }
                    LogsViewState.SectionInfo(
                        logs = logs.subList(startIndex, logIndex),
                        meta = info.meta,
                    )
                }
            }
        }
        LogViewerLogger.d { "SectionInfoEnricher#groupLogsByRun() done at $time" }
        return result
    }

    data class EnrichResult(
        val searchIndexProgress: LogIndexProgress,
        val sectionInfos: List<LogsViewState.SectionInfo>,
    )
}
