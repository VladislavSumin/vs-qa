package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.generic.GenericLogParser
import java.time.Instant
import java.time.LocalDate

@Suppress("MagicNumber")
internal object AnimeEmbeddedLogParser : GenericLogParser() {

    /**
     * Ручная проверка заголовка лога формата YYYY-MM-DDTHH:SS±HH:MM HH:MM:SS.mmm THREAD L TAG MESSAGE.
     * Вместо Regex.matchEntire() — ручная проверка символов-разделителей на фиксированных позициях.
     *
     * Было:
     * Regex("^(\\d{4}-\\d{2}-\\d{2}T(\\+\\d{2}:\\d{2}|Z)) \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) ([^ ]+) (.*)")
     */
    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    override fun tryParseHeader(line: String): ParsedHeader? {
        if (line.length < 25) return null

        if (line[4] != '-' || line[7] != '-' || line[10] != 'T') return null

        val tzChar = line[11]
        val spaceAfterTz: Int
        if (tzChar == '+' || tzChar == '-') {
            spaceAfterTz = 17
            if (line.length <= 17 || line[17] != ' ') return null
        } else if (tzChar == 'Z') {
            spaceAfterTz = 12
            if (line.length <= 12 || line[12] != ' ') return null
        } else {
            return null
        }

        val timeStart = spaceAfterTz + 1
        val timeEnd = timeStart + 12 // HH:MM:SS.mmm = 12 chars
        if (line.length < timeEnd) return null
        if (line[timeStart + 2] != ':' || line[timeStart + 5] != ':' || line[timeStart + 8] != '.') return null
        if (line[timeEnd] != ' ') return null

        val threadStart = timeEnd + 1
        val threadEnd = findSpace(line, threadStart)
        if (threadEnd == threadStart || threadEnd == -1) return null
        if (line[threadEnd] != ' ') return null

        val levelStart = threadEnd + 1
        val levelEnd = levelStart + 1
        if (line.length <= levelEnd || line[levelEnd] != ' ') return null

        val tagStart = levelEnd + 1
        val tagEnd = findSpace(line, tagStart)
        if (tagEnd == tagStart || tagEnd == -1) return null
        if (line[tagEnd] != ' ') return null

        val messageStart = tagEnd + 1
        val messageEnd = line.length - 1

        return ParsedHeader(
            time = LogRange(0, timeEnd - 1),
            timeDate = LogRange(0, spaceAfterTz - 1),
            processId = null,
            thread = LogRange(threadStart, threadEnd - 1),
            level = LogRange(levelStart, levelEnd - 1),
            tag = LogRange(tagStart, tagEnd - 1),
            message = LogRange(messageStart, messageEnd),
            levelAlias = line[levelStart].toString(),
            timeInstantValue = line.substring(0, timeEnd),
        )
    }

    /**
     * Ручной парсинг Instant из строки времени формата YYYY-MM-DDTHH:SS±HH:MM HH:MM:SS.mmm.
     * Вместо DateTimeFormatter с OffsetDateTime.parse() — извлечение компонентов по известным позициям
     * и вычисление через LocalDate.toEpochDay().
     *
     * Было: DateTimeFormatterBuilder()
     *           .append(ISO_LOCAL_DATE).appendLiteral('T').appendZoneOrOffsetId()...
     *       OffsetDateTime.parse(value, DATE_FORMATTER).toInstant()
     */
    @Suppress("MagicNumber", "ReturnCount")
    override fun parseInstant(value: String): Instant {
        val year = value.substring(0, 4).toInt()
        val month = value.substring(5, 7).toInt()
        val day = value.substring(8, 10).toInt()

        val tzChar = value[11]
        val tzOffsetSeconds: Int
        val spaceIndex: Int
        if (tzChar == '+' || tzChar == '-') {
            val sign = if (tzChar == '-') -1 else 1
            val tzHours = value.substring(12, 14).toInt()
            val tzMinutes = value.substring(15, 17).toInt()
            tzOffsetSeconds = sign * (tzHours * 3600 + tzMinutes * 60)
            spaceIndex = 17
        } else {
            tzOffsetSeconds = 0
            spaceIndex = 12
        }

        val hours = value.substring(spaceIndex + 1, spaceIndex + 3).toInt()
        val minutes = value.substring(spaceIndex + 4, spaceIndex + 6).toInt()
        val seconds = value.substring(spaceIndex + 7, spaceIndex + 9).toInt()
        val millis = value.substring(spaceIndex + 10, spaceIndex + 13).toInt()

        val epochDay = LocalDate.of(year, month, day).toEpochDay()
        val epochSecond = epochDay * 86_400 + hours * 3_600 + minutes * 60 + seconds - tzOffsetSeconds
        return Instant.ofEpochSecond(epochSecond, millis * 1_000_000L)
    }

    private fun findSpace(str: String, start: Int): Int {
        var i = start
        while (i < str.length && str[i] != ' ') i++
        return i
    }

    override fun onOrphanLine(line: String) {
        // TODO show user notification about unexpected log format
        AnimeLogger.e { "Orphan line before first header ignored: ${line.take(100)}" }
    }
}
