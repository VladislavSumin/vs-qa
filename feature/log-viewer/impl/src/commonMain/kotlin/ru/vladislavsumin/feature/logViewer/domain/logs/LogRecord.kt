package ru.vladislavsumin.feature.logViewer.domain.logs

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import java.time.Instant

data class LogRecord(
    val order: Int,
    val raw: String,
    val time: IntRange,
    val timeInstant: Instant,
    val level: IntRange,
    val processId: IntRange?,
    val thread: IntRange,
    val tag: IntRange,
    val message: IntRange,

    val searchHighlights: List<IntRange>?,
    val logLevel: LogLevel,
)
