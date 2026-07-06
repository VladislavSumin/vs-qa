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

    protected abstract fun tryParseHeader(line: String): ParsedHeader?
    protected abstract fun parseInstant(value: String): Instant

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
            }
        }

        dumpCache()
    }
}
