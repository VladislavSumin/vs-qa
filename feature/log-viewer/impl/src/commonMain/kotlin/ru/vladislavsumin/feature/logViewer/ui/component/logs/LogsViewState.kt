package ru.vladislavsumin.feature.logViewer.ui.component.logs

import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord

internal data class LogsViewState(
    val logs: List<List<LogRecord>>,
    val rawLogs: List<LogRecord>,
    val showRunNumbers: Boolean,
    val maxLogNumberDigits: Int,
) {
    companion object {
        val STUB = LogsViewState(
            logs = emptyList(),
            rawLogs = emptyList(),
            showRunNumbers = false,
            maxLogNumberDigits = 0,
        )
    }
}
