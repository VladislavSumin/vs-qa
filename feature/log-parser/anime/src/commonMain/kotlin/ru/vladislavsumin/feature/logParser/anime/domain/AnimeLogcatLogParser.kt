package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.Instant
import java.time.LocalDate
import java.time.Year

@Suppress("MagicNumber")
object AnimeLogcatLogParser {
    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        var cache: RawLogRecord? = null
        var singleLineRaw: String? = null
        var rawBuilder: StringBuilder? = null
        var linesCount = 0

        fun dumpCache() {
            cache?.let { cache ->
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
                result.add(record)
                singleLineRaw = null
                rawBuilder = null
                linesCount = 0
            }
            cache = null
        }

        for (line in lines) {
            val header = tryParseLogcatHeader(line)
            if (header != null) {
                dumpCache()
                val headerLine = "${header.date} ${header.pid}:${header.tid} ${header.levelAlias} ${header.tag} "
                singleLineRaw = headerLine
                linesCount = 1

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

    private class LogcatHeader(
        val date: String,
        val pid: String,
        val tid: String,
        val levelAlias: String,
        val tag: String,
    )

    @Suppress("ReturnCount", "ComplexCondition", "CyclomaticComplexMethod")
    private fun tryParseLogcatHeader(line: String): LogcatHeader? {
        // Format: [ MM-DD HH:MM:SS.mmm PID:TID L/TAG ]
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

    @Suppress("MagicNumber")
    private fun parseLogcatInstant(date: String): Instant {
        val month = date.substring(0, 2).toInt()
        val day = date.substring(3, 5).toInt()
        val hour = date.substring(6, 8).toInt()
        val minute = date.substring(9, 11).toInt()
        val second = date.substring(12, 14).toInt()
        val millis = date.substring(15, 18).toInt()

        val epochDay = LocalDate.of(Year.now().value, month, day).toEpochDay()
        val epochSecond = epochDay * 86_400 + hour * 3_600 + minute * 60 + second
        return Instant.ofEpochSecond(epochSecond, millis * 1_000_000L)
    }

    private const val LOGCAT_META_HEADER = "--------- "
}
