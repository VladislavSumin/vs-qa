package ru.vladislavsumin.feature.logViewer.ui.component.logs

import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord

internal data class LogsViewState(
    val logs: List<SectionInfo>,
    val rawLogs: List<LogRecord>,
    val currentSelectedItemOrder: Int,
    val showRunNumbers: Boolean,
    val maxLogNumberDigits: Int,
) {

    data class SectionInfo(
        val logs: List<LogRecord>,
        val meta: Map<String, String>?,
    )

    companion object {
        val STUB = LogsViewState(
            logs = emptyList(),
            rawLogs = emptyList(),
            currentSelectedItemOrder = -1,
            showRunNumbers = false,
            maxLogNumberDigits = 0,
        )
    }
}
