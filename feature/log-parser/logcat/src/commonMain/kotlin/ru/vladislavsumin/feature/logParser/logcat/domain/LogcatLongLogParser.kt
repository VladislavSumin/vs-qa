package ru.vladislavsumin.feature.logParser.logcat.domain

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.Instant
import java.time.LocalDate
import java.time.Year

/**
 * Парсер logcat формата `-v long` (`[ MM-DD HH:MM:SS.mmm PID:TID L/TAG ]`).
 */
@Suppress("MagicNumber")
object LogcatLongLogParser {
    fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        val session = Session()
        for (line in lines) {
            session.onLine(line)?.let(result::add)
        }
        session.finish()?.let(result::add)
    }

    /**
     * Построчный stateful обработчик logcat формата.
     *
     * Запись коммитится только при появлении заголовка следующей записи (или при вызове [finish]).
     */
    class Session {
        private var cache: RawLogRecord? = null
        private var singleLineRaw: String? = null
        private var rawBuilder: StringBuilder? = null
        private var linesCount = 0

        /**
         * Обрабатывает очередную строку лога.
         *
         * @return завершенную предыдущую запись, если [line] оказалась заголовком новой записи, иначе null.
         */
        fun onLine(line: String): RawLogRecord? {
            val header = tryParseLogcatHeader(line)
            if (header != null) {
                val completed = dumpCache()
                val headerLine = "${header.date} ${header.pid}:${header.tid} ${header.levelAlias} ${header.tag} "
                singleLineRaw = headerLine

                val dateRange = LogRange(0, header.date.length - 1)
                val pidRange = LogRange(dateRange.last + 2, dateRange.last + 1 + header.pid.length)
                val tidRange = LogRange(pidRange.last + 2, pidRange.last + 1 + header.tid.length)
                val levelRange = LogRange(tidRange.last + 2, tidRange.last + 1 + header.levelAlias.length)
                val tagRange = LogRange(levelRange.last + 2, levelRange.last + 1 + header.tag.length)

                cache = RawLogRecord(
                    raw = "",
                    time = dateRange,
                    timeDate = LogRange(0, 0),
                    timeInstant = parseLogcatInstant(header.date),
                    processId = pidRange,
                    thread = tidRange,
                    level = levelRange,
                    tag = tagRange,
                    message = LogRange.EMPTY,
                    logLevel = LogLevel.fromAlias(header.levelAlias)
                        ?: error("UNKNOWN LEVEL ${header.levelAlias}"),
                    lines = 1,
                )
                return completed
            } else if (cache != null) {
                if (rawBuilder == null) {
                    rawBuilder = StringBuilder()
                    rawBuilder!!.append(singleLineRaw!!)
                    singleLineRaw = null
                }
                rawBuilder!!.appendLine(line)
                linesCount++
            }
            return null
        }

        /**
         * Завершает обработку, возвращая последнюю накопленную запись, если она есть.
         */
        fun finish(): RawLogRecord? = dumpCache()

        private fun dumpCache(): RawLogRecord? {
            val cache = cache ?: return null
            this.cache = null

            val raw: String
            if (rawBuilder != null) {
                val builder = rawBuilder!!
                builder.deleteCharAt(builder.length - 1)

                while (true) {
                    val index = builder.lastIndexOf('\n')
                    if (index == -1) error("Unexpected format!")

                    if (
                        index + LOGCAT_META_HEADER.length < builder.length &&
                        builder.substring(index + 1, index + LOGCAT_META_HEADER.length + 1) == LOGCAT_META_HEADER
                    ) {
                        linesCount--
                        builder.deleteRange(index, builder.length)
                    } else {
                        break
                    }
                }

                if (builder[builder.length - 1] == '\n') {
                    builder.deleteCharAt(builder.length - 1)
                    linesCount--
                }
                raw = builder.toString()
            } else {
                raw = singleLineRaw!!.substring(0, singleLineRaw!!.length - 1)
            }

            val record = cache.copy(
                raw = raw,
                message = LogRange(
                    start = cache.tag.last + 2,
                    endInclusive = raw.length - 1,
                ),
                lines = linesCount,
            )
            check(record.lines == record.raw.lines().size) {
                "Debug compare real && calculated string count failed, please report to author"
            }
            singleLineRaw = null
            rawBuilder = null
            linesCount = 0
            return record
        }
    }

    private class LogcatHeader(
        val date: String,
        val pid: String,
        val tid: String,
        val levelAlias: String,
        val tag: String,
    )

    /**
     * Ручная проверка заголовка logcat формата [ MM-DD HH:MM:SS.mmm PID:TID L/TAG ].
     * Вместо Regex.matchEntire() — ручной поиск скобок, двоеточий, слэша и разделителей.
     *
     * Было: Regex("^\\[ (\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) \\s*(\\d+):\\s*(\\d+) ([A-Z])/([^ ]+)\\s*]")
     */
    @Suppress("ReturnCount", "ComplexCondition", "CyclomaticComplexMethod")
    private fun tryParseLogcatHeader(line: String): LogcatHeader? {
        if (line.length < 22) return null
        if (line[0] != '[' || line[1] != ' ') return null

        val date = line.substring(2, 20)
        if (line[20] != ' ') return null
        // Validate date separators
        if (line[4] != '-' || line[7] != ' ' || line[10] != ':' || line[13] != ':' || line[16] != '.') return null

        val pidTidStart = findNonSpace(line, 21)
        if (pidTidStart == -1) return null
        val pidTidEnd = findSpace(line, pidTidStart)
        if (pidTidEnd == -1) return null

        val pidColonTid = line.substring(pidTidStart, pidTidEnd)
        val colonIndex = pidColonTid.indexOf(':')
        if (colonIndex == -1) return null
        val pid = pidColonTid.substring(0, colonIndex)
        val tid = pidColonTid.substring(colonIndex + 1)
        if (pid.isEmpty() || tid.isEmpty()) return null

        val levelTagPos = findNonSpace(line, pidTidEnd)
        if (levelTagPos == -1) return null
        val bracketPos = line.indexOf(']', levelTagPos)
        if (bracketPos == -1) return null

        val levelTagRaw = line.substring(levelTagPos, bracketPos).trim()
        val slashIndex = levelTagRaw.indexOf('/')
        if (slashIndex == -1) return null
        val levelAlias = levelTagRaw.substring(0, slashIndex)
        val tag = levelTagRaw.substring(slashIndex + 1)
        if (levelAlias.isEmpty() || tag.isEmpty()) return null

        return LogcatHeader(date, pid, tid, levelAlias, tag)
    }

    private fun findSpace(str: String, start: Int): Int {
        var i = start
        while (i < str.length && str[i] != ' ') i++
        return if (i >= str.length) -1 else i
    }

    private fun findNonSpace(str: String, start: Int): Int {
        var i = start
        while (i < str.length && str[i] == ' ') i++
        return if (i >= str.length) -1 else i
    }

    /**
     * Ручной парсинг Instant из строки времени logcat формата MM-DD HH:MM:SS.mmm.
     * Вместо DateTimeFormatter с OffsetDateTime.parse() — извлечение компонентов по известным позициям
     * и вычисление через LocalDate.toEpochDay(). Формат logcat фиксирован, позиции известны.
     * Год берётся текущий, offset=0 (UTC) — поведение идентично старому.
     *
     * Было: DateTimeFormatterBuilder()
     *           .parseDefaulting(YEAR, Year.now().value)
     *           .parseDefaulting(OFFSET_SECONDS, 0)
     *       OffsetDateTime.parse(date, DATE_FORMATTER).toInstant()
     */
    @Suppress("MagicNumber")
    private fun parseLogcatInstant(date: String): Instant {
        val month = parseIntFromChars(date, 0, 2)
        val day = parseIntFromChars(date, 3, 2)
        val hour = parseIntFromChars(date, 6, 2)
        val minute = parseIntFromChars(date, 9, 2)
        val second = parseIntFromChars(date, 12, 2)
        val millis = parseIntFromChars(date, 15, 3)

        val epochDay = LocalDate.of(Year.now().value, month, day).toEpochDay()
        val epochSecond = epochDay * 86_400 + hour * 3_600 + minute * 60 + second
        return Instant.ofEpochSecond(epochSecond, millis * 1_000_000L)
    }

    private fun parseIntFromChars(str: String, start: Int, length: Int): Int {
        var result = 0
        var i = start
        val end = start + length
        while (i < end) {
            result = result * 10 + (str[i].code - '0'.code)
            i++
        }
        return result
    }

    private const val LOGCAT_META_HEADER = "--------- "
}
