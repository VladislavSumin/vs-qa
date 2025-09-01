package ru.vladislavsumin.qa.domain.logs

data class LogRecord(
    val order: Int,
    val raw: String,
    val time: IntRange,
    val level: IntRange,
    val thread: IntRange,
    val tag: IntRange,
    val message: IntRange,

    val searchHighlight: IntRange?,
)
