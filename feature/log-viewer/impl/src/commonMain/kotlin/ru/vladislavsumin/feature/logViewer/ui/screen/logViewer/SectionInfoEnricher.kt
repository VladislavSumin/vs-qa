package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndexProgress
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsViewState
import kotlin.time.measureTimedValue

internal class SectionInfoEnricher {
    // TODO map latest + cache
    fun enrich(searchIndex: Flow<LogIndexProgress>): Flow<EnrichResult> = searchIndex.map { index ->
        EnrichResult(
            searchIndexProgress = index,
            sectionInfos = groupLogsByRun(
                index.lastSuccessIndex.logs,
                index.lastSuccessIndex.runIdOrders,
            ),
        )
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
        LogViewerLogger.d { "groupLogsByRun() done at $time" }
        return result
    }

    data class EnrichResult(
        val searchIndexProgress: LogIndexProgress,
        val sectionInfos: List<LogsViewState.SectionInfo>,
    )
}
