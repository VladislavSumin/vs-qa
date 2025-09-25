package ru.vladislavsumin.feature.logViewer.ui.component.logs

import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord

internal data class LogsViewState(
    val logs: List<List<LogRecord>>,
    val rawLogs: List<LogRecord>,
    val maxLogNumberDigits: Int,
)
