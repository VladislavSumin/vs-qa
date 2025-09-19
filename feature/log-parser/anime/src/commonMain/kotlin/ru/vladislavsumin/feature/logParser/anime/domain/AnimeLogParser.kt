package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.core.logger.api.logger
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogParser
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.nio.file.Path
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.extension
import kotlin.system.measureTimeMillis

internal class AnimeLogParser : LogParser {
    override suspend fun parseLog(filePath: Path): List<RawLogRecord> {
        // Производительность тут примерно 1,2кк строк в секунду, поэтому дополнительные оптимизации пока не нужны.
        LogParserLogger.i { "Start parsing file $filePath with ${this.javaClass.simpleName}" }

        val result = mutableListOf<RawLogRecord>()
        val totalParseTime = measureTimeMillis {
            if (filePath.extension == "zip") {
                parseZip(filePath, result)
            } else {
                parseLogFile(filePath, result)
            }
        }
        LogParserLogger.d { "Parsed file $filePath at ${totalParseTime}ms. logs = ${result.size}}" }
        return result
    }

    private fun parseZip(filePath: Path, result: MutableList<RawLogRecord>) {
        val zip = ZipFile(filePath.absolutePathString())
        val names = zip.entries().toList()
            // Фильтр нужен для корректной работы с перепакованными на macos архивами.
            // Отрезаем мета информацию из архива.
            .filter { !it.isDirectory && !it.name.startsWith("__MACOSX") }
            .map { it.name }
            .sorted()
        names.forEach {
            zip.getInputStream(zip.getEntry(it)).use { internalZipStream ->
                ZipInputStream(internalZipStream).use { zipStream ->
                    zipStream.nextEntry!!
                    val lines = zipStream.bufferedReader().lineSequence()
                    parseLines(lines, result)
                }
            }
        }
    }

    private fun parseLogFile(filePath: Path, result: MutableList<RawLogRecord>) {
        val lines = filePath.bufferedReader().lineSequence()
        parseLines(lines, result)
    }

    @Suppress("MagicNumber")
    private fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        var cache: RawLogRecord? = null
        val rawBuilder: StringBuilder = StringBuilder()

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
                )
                result.add(record)
                rawBuilder.clear()
            }
            cache = null
        }

        var order = result.size
        for (line in lines) {
            val matches = LOG_REGEX.matchEntire(line)
            if (matches != null) {
                dumpCache()
                cache = RawLogRecord(
                    order = ++order,
                    raw = "",
                    time = matches.groups[1]!!.range,
                    timeInstant = OffsetDateTime.parse(
                        matches.groups[1]!!.value,
                        DATE_FORMATTER,
                    ).toInstant(),
                    thread = matches.groups[2]!!.range,
                    level = matches.groups[3]!!.range,
                    tag = matches.groups[4]!!.range,
                    message = matches.groups[5]!!.range,
                    logLevel = LogLevel.fromAlias(matches.groups[3]!!.value)
                        ?: error("UNKNOWN LEVEL ${matches.groups[3]!!.value}"),
                    lines = 1,
                )
            }
            rawBuilder.appendLine(line)
        }

        dumpCache()
    }

    companion object {
        @Suppress("MaxLineLength")
        private val LOG_REGEX = Regex(
            pattern = "^(\\d{4}-\\d{2}-\\d{2}T\\+\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) ([^ ]+) (.*)",
        )

        private val DATE_FORMATTER = DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendOffset("+HH:MM", "+00:00")
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
            .toFormatter()
    }
}

private val LogParserLogger = logger("anime-log-parser")
