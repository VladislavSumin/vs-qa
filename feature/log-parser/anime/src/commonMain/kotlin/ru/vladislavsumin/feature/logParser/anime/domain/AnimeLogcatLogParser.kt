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
                // Удаляем \n добавленную нашей логикой
                rawBuilder.deleteCharAt(rawBuilder.length - 1)
                // linesCount-- линия добавляемая нашей логикой тут не учитывается.

                // Удаляем служебную мету logcat
                while (true) {
                    val index = rawBuilder.lastIndexOf('\n')
                    if (index == -1) error("Unexpected format!")

                    if (
                        index + LOGCAT_META_HEADER.length < rawBuilder.length &&
                        rawBuilder.substring(index + 1, index + LOGCAT_META_HEADER.length + 1) == LOGCAT_META_HEADER
                    ) {
                        linesCount--
                        rawBuilder.deleteRange(index, rawBuilder.length)
                    } else {
                        break
                    }
                }

                // Дополнительная проверка на косяки лога. Так как запись могли прибить прямо на середине строки.
                // Тогда мы можем получить запись такого вида:
                // at x.x.x
                // at x--------- beginning of crash
                // [ XX-XX XX:XX:XX.XXX XXXXX:XXXXX E/AndroidRuntime ]
                // Вот тут нам и поможет эта проверка, запись будет кривой конечно, но тут уже ничего не сделать.
                if (rawBuilder[rawBuilder.length - 1] == '\n') {
                    // Удаляем \n добавленную форматом long logcat
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
                    "Debug compare real && calculated string count failed, please report to author"
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
                val pidRange = IntRange(dateRange.last + 2, dateRange.last + 1 + pid.length)
                val tid = matches.groups[3]!!.value
                val tidRange = IntRange(pidRange.last + 2, pidRange.last + 1 + tid.length)
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
                    timeDate = IntRange(0, 0), // TODO поддержать
                    timeInstant = OffsetDateTime.parse(date, DATE_FORMATTER).toInstant(),
                    processId = pidRange,
                    thread = tidRange,
                    level = levelRange,
                    tag = tagRange,
                    message = IntRange.EMPTY,
                    logLevel = LogLevel.fromAlias(matches.groups[4]!!.value)
                        ?: error("UNKNOWN LEVEL ${matches.groups[4]!!.value}"),
                    lines = 1,
                )
            } else {
                // before handleClick, view hierarchy ...
                if (cache != null) {
                    rawBuilder.appendLine(line)
                    linesCount++
                }
            }
        }

        dumpCache()
    }

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
        // Logcat не пишет год, поэтому для вычислений считаем что год текущий. Тут есть нюанс (пасхалка). Новогодние
        // логи с переходом через год могут распознаваться некорректно. Это не должно приводить к крашу, так как нет
        // соглашения, что время последующей записи не может быть меньше времени прошлой записи. Более того такое
        // поведения является нормальным для асинхронных логов.
        .parseDefaulting(ChronoField.YEAR, Year.now().value.toLong())
        .parseDefaulting(ChronoField.OFFSET_SECONDS, 0) // UTC по умолчанию
        .toFormatter()

    private const val LOGCAT_META_HEADER = "--------- "
}
