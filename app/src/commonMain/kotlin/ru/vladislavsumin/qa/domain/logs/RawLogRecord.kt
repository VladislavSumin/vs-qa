package ru.vladislavsumin.qa.domain.logs

import java.time.Instant

/**
 * Сырая log запись.
 *
 * @param line номер первой строки с которой начинается этот лог.
 * @param order номер лога (так как одна запись может занимать несколько строк, то этот параметер в общем случаее
 * не равен параметру [line].
 * @param raw сырая log запись не разбитая на подкомпоненты.
 *
 * Все прочие параметры являются слайсами [raw] строки. Так сделано для экономии памяти что бы не держать в памяти
 * несколько похожих компий одной строки.
 *
 * @param lines общее количество строк в записи.
 */
data class RawLogRecord(
    // val line: Int,
    val order: Int,
    val raw: String,
    val time: IntRange,
    val timeInstant: Instant,
    val level: IntRange,
    val thread: IntRange,
    val tag: IntRange,
    val message: IntRange,

    val logLevel: LogLevel,
    val lines: Int,
)
