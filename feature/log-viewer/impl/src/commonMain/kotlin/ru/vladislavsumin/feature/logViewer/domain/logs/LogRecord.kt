package ru.vladislavsumin.feature.logViewer.domain.logs

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import java.time.Instant

/**
 * @param order порядковый номер записи в полном списке логов до всех фильтраций
 * @param raw сырая лог запись.
 *
 * @param time часть, где расположено время.
 * @param time часть, где расположена дата, эта часть **находится внутри** [time].
 * @param level часть, где расположен уровень лога.
 * @param processId часть, где расположен PID.
 * @param thread часть, где расположен TID или название потока.
 * @param tag часть, где расположен тэг
 * @param message часть, где расположено сообщение лога
 *
 * @param timeInstant время [time] в удобном формате.
 * @param logLevel уровень логов [level] в удобном формате.
 * @param searchHighlights выделения поисковых вхождений.
 */
data class LogRecord(
    val order: Int,
    val raw: String,

    val time: IntRange,
    val timeDate: IntRange,

    val level: IntRange,
    val processId: IntRange?,
    val thread: IntRange,
    val tag: IntRange,
    val message: IntRange,

    val timeInstant: Instant,
    val logLevel: LogLevel,
    val searchHighlights: List<IntRange>?,
)
