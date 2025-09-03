package ru.vladislavsumin.qa.domain.logs

import java.time.Instant

data class LogRecord(
    val order: Int,
    val raw: String,
    val time: IntRange,
    val timeInstant: Instant,
    val level: IntRange,
    val thread: IntRange,
    val tag: IntRange,
    val message: IntRange,

    val searchHighlight: IntRange?,
)
