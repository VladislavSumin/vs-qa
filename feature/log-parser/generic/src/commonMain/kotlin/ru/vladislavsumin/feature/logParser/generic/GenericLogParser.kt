package ru.vladislavsumin.feature.logParser.generic

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.Instant

abstract class GenericLogParser {
    protected class ParsedHeader(
        val time: LogRange,
        val timeDate: LogRange,
        val processId: LogRange?,
        val thread: LogRange,
        val level: LogRange,
        val tag: LogRange,
        val message: LogRange,
        val levelAlias: String,
        val timeInstantValue: String,
    )

    /**
     * Ручная проверка строки на соответствие формату заголовка лога и извлечение полей.
     * Вместо [Regex.matchEntire] — ручная проверка разделителей на фиксированных позициях.
     *
     * Было: protected abstract val logRegex: Regex
     *       val matches = logRegex.matchEntire(line)
     *       matches.groups[timeGroupId]!!.range и т.д.
     */
    protected abstract fun tryParseHeader(line: String): ParsedHeader?

    /**
     * Ручной парсинг Instant из строки времени заголовка лога.
     * Вместо [java.time.format.DateTimeFormatter] с [java.time.OffsetDateTime.parse] —
     * извлечение компонентов даты по известным позициям и вычисление через [java.time.LocalDate.toEpochDay].
     *
     * Было: abstract val dateTimeFormatter: DateTimeFormatter
     *       OffsetDateTime.parse(value, dateTimeFormatter).toInstant()
     */
    protected abstract fun parseInstant(value: String): Instant

    protected open fun onOrphanLine(line: String) {}

    fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        var cache: RawLogRecord? = null
        var singleLineRaw: String? = null
        var rawBuilder: StringBuilder? = null
        var linesCount = 0

        fun dumpCache() {
            cache?.let { cache ->
                val raw: String
                if (rawBuilder != null) {
                    rawBuilder!!.deleteCharAt(rawBuilder!!.length - 1)
                    raw = rawBuilder!!.toString()
                } else {
                    raw = singleLineRaw!!
                }
                val record = cache.copy(
                    raw = raw,
                    message = LogRange(
                        start = cache.tag.last + 2,
                        endInclusive = raw.length - 1,
                    ),
                    lines = linesCount,
                )
                result.add(record)
                singleLineRaw = null
                rawBuilder = null
                linesCount = 0
            }
            cache = null
        }

        for (line in lines) {
            val header = tryParseHeader(line)
            if (header != null) {
                dumpCache()
                cache = RawLogRecord(
                    raw = "",
                    time = header.time,
                    timeDate = header.timeDate,
                    timeInstant = parseInstant(header.timeInstantValue),
                    processId = header.processId,
                    thread = header.thread,
                    level = header.level,
                    tag = header.tag,
                    message = header.message,
                    logLevel = LogLevel.fromAlias(header.levelAlias)
                        ?: error("UNKNOWN LEVEL ${header.levelAlias}"),
                    lines = 1,
                )
                singleLineRaw = line
                linesCount = 1
            } else if (linesCount > 0) {
                if (rawBuilder == null) {
                    rawBuilder = StringBuilder()
                    rawBuilder!!.appendLine(singleLineRaw!!)
                    singleLineRaw = null
                }
                rawBuilder!!.appendLine(line)
                linesCount++
            } else {
                onOrphanLine(line)
            }
        }

        dumpCache()
    }
}
