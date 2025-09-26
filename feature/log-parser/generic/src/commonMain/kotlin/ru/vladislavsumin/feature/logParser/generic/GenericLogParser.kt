package ru.vladislavsumin.feature.logParser.generic

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

abstract class GenericLogParser {
    protected abstract val logRegex: Regex
    protected abstract val timeGroupId: Int
    protected abstract val threadGroupId: Int
    protected abstract val levelGroupId: Int
    protected abstract val tagGroupId: Int
    protected abstract val messageGroupId: Int

    abstract val dateTimeFormatter: DateTimeFormatter

    fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        var cache: RawLogRecord? = null
        val rawBuilder: StringBuilder = StringBuilder()
        var linesCount = 0

        fun dumpCache() {
            cache?.let { cache ->
                // Remove last new line
                rawBuilder.deleteCharAt(rawBuilder.length - 1)

                val raw = rawBuilder.toString()
                val record = cache.copy(
                    raw = raw,
                    message = IntRange(
                        start = cache.tag.last + 2, // После тега пробел, его исключаем.
                        endInclusive = raw.length - 1,
                    ),
                    lines = linesCount,
                )
                result.add(record)
                rawBuilder.clear()
                linesCount = 0
            }
            cache = null
        }

        for (line in lines) {
            val matches = logRegex.matchEntire(line)
            if (matches != null) {
                dumpCache()
                cache = RawLogRecord(
                    raw = "",
                    time = matches.groups[timeGroupId]!!.range,
                    timeInstant = OffsetDateTime.parse(
                        matches.groups[timeGroupId]!!.value,
                        dateTimeFormatter,
                    ).toInstant(),
                    thread = matches.groups[threadGroupId]!!.range,
                    level = matches.groups[levelGroupId]!!.range,
                    tag = matches.groups[tagGroupId]!!.range,
                    message = matches.groups[messageGroupId]!!.range,
                    logLevel = LogLevel.fromAlias(matches.groups[levelGroupId]!!.value)
                        ?: error("UNKNOWN LEVEL ${matches.groups[levelGroupId]!!.value}"),
                    lines = 1,
                )
            }
            rawBuilder.appendLine(line)
            linesCount++
        }

        dumpCache()
    }
}
