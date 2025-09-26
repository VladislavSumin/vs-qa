package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.OffsetDateTime
import java.time.Year
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Suppress("MagicNumber")
object AnimeLogcatLogParser {
    fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        var cache: RawLogRecord? = null
        val rawBuilder: StringBuilder = StringBuilder()
        var linesCount = 0

        fun dumpCache() {
            cache?.let { cache ->
                rawBuilder.deleteCharAt(rawBuilder.length - 1)
                while (rawBuilder[rawBuilder.length - 1] == '\n') {
                    rawBuilder.deleteCharAt(rawBuilder.length - 1)
                    linesCount--
                }

                val raw = rawBuilder.toString()
                val record = cache.copy(
                    raw = raw,
                    message = IntRange(
                        start = cache.tag.last + 2, // После тега пробел, его исключаем.
                        endInclusive = raw.length - 1,
                    ),
                    lines = linesCount,
                )
                check(record.lines == record.raw.lines().size) {
                    "left = ${record.lines}, r=${record.raw.lines().size}\n$raw"
                }
                result.add(record)
                rawBuilder.clear()
                linesCount = 0
            }
            cache = null
        }

        for (line in lines) {
            val matches = LOG_REGEX.matchEntire(line)
            if (matches != null) {
                dumpCache()

                val date = matches.groups[1]!!.value
                val dateRange = IntRange(0, date.length - 1)
                val pid = matches.groups[2]!!.value
                val tid = matches.groups[3]!!.value
                val tidRange = IntRange(
                    start = dateRange.last + 2 + pid.length + 1,
                    endInclusive = dateRange.last + 2 + pid.length + tid.length,
                )
                val level = matches.groups[4]!!.value
                val levelRange = IntRange(tidRange.last + 2, tidRange.last + 1 + level.length)
                val tag = matches.groups[5]!!.value
                val tagRange = IntRange(levelRange.last + 2, levelRange.last + 1 + tag.length)

                // Переписываем строчку на нормальный формат.
                val line = "$date $pid:$tid $level $tag "
                rawBuilder.append(line)

                cache = RawLogRecord(
                    raw = "",
                    time = dateRange,
                    timeInstant = OffsetDateTime.parse(date, DATE_FORMATTER).toInstant(),
                    thread = tidRange,
                    level = levelRange,
                    tag = tagRange,
                    message = IntRange.EMPTY,
                    logLevel = LogLevel.fromAlias(matches.groups[4]!!.value)
                        ?: error("UNKNOWN LEVEL ${matches.groups[4]!!.value}"),
                    lines = 1,
                )
            } else {
                if (cache != null && !line.startsWith("--------- ")) {
                    rawBuilder.appendLine(line)
                    linesCount++
                }
            }
        }

        dumpCache()
    }

    @Suppress("MaxLineLength")
    private val LOG_REGEX = Regex(
        pattern = "^\\[ (\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) \\s*(\\d+):\\s*(\\d+) ([A-Z])/([^ ]+)\\s*]",
    )

    private val DATE_FORMATTER = DateTimeFormatterBuilder()
        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral(' ')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
        .parseDefaulting(ChronoField.YEAR, Year.now().value.toLong())
        .parseDefaulting(ChronoField.OFFSET_SECONDS, 0) // UTC по умолчанию
        .toFormatter()
}
