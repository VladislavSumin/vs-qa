package ru.vladislavsumin.feature.logViewer.ui.component.logs

import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo

internal data class LogsViewState(
    val logs: List<SectionInfo>,
    val rawLogs: List<LogRecord>,
    val runIdOrders: List<RunIdInfo>?,
    val currentSelectedItemOrder: Int,
    val showRunNumbers: Boolean,
    val maxLogNumberDigits: Int,
    val stripDate: Boolean,
) {

    data class SectionInfo(
        val logs: List<LogRecord>,
        val meta: Map<String, String>?,
    )

    companion object {
        val STUB = LogsViewState(
            logs = emptyList(),
            rawLogs = emptyList(),
            runIdOrders = null,
            currentSelectedItemOrder = -1,
            showRunNumbers = false,
            maxLogNumberDigits = 0,
            stripDate = false,
        )
    }
}
