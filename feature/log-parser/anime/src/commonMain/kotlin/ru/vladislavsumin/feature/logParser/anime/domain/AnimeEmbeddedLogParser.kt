package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.generic.GenericLogParser
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Suppress("MagicNumber")
internal object AnimeEmbeddedLogParser : GenericLogParser() {
    @Suppress("MaximumLineLength", "MaxLineLength")
    private val LOG_REGEX = Regex(
        pattern = "^((\\d{4}-\\d{2}-\\d{2}T(\\+\\d{2}:\\d{2}|Z)) \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) ([^ ]+) (.*)",
    )

    private val DATE_FORMATTER = DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendZoneOrOffsetId()
        .appendLiteral(' ')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
        .toFormatter()

    override val logRegex: Regex = LOG_REGEX
    override val timeGroupId: Int = 1
    override val timeDateGroupId: Int = 3
    override val threadGroupId: Int = 4
    override val processIdGroupId: Int? = null
    override val levelGroupId: Int = 5
    override val tagGroupId: Int = 6
    override val messageGroupId: Int = 7
    override val dateTimeFormatter: DateTimeFormatter = DATE_FORMATTER
}
