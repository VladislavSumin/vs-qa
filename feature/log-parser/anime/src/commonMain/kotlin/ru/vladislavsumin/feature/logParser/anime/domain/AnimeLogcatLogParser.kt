package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.generic.GenericLogParser
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Suppress("MagicNumber")
object AnimeLogcatLogParser : GenericLogParser() {
    @Suppress("MaxLineLength")
    private val LOG_REGEX = Regex(
        pattern = "^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) (\\d+) (\\d+) ([A-Z]) ([^ ]+)\\s*: (.*)",
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

    override val logRegex: Regex = LOG_REGEX
    override val timeGroupId: Int = 1
    override val threadGroupId: Int = 3
    override val levelGroupId: Int = 4
    override val tagGroupId: Int = 5
    override val messageGroupId: Int = 6
    override val dateTimeFormatter: DateTimeFormatter = DATE_FORMATTER
}
