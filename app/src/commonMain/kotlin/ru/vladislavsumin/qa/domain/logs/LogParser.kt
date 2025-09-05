package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogParserLogger
import ru.vladislavsumin.qa.domain.proguard.ProguardInteractor
import ru.vlasidlavsumin.core.stacktraceParser.StacktraceParser
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

interface LogParser {
    fun parseLog(filePath: Path): List<RawLogRecord>
}

class AnimeLogParser(
    private val proguardInteractor: ProguardInteractor?,
) : LogParser {
    override fun parseLog(filePath: Path): List<RawLogRecord> {
        // Производительность тут примеррно 1,2кк строк в секунду, поэтому дополнительные оптимизации пока не нужны.
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
        return if (proguardInteractor != null) {
            result.map {
                val newMsg = tryDeobfuscateStackTrace(it.raw.substring(it.message))

                if (newMsg != null) {
                    it.copy(
                        raw = it.raw.replaceRange(it.message, newMsg),
                        message = IntRange(it.message.start, it.message.start + newMsg.length - 1),
                    )
                } else {
                    it
                }
            }
        } else {
            result
        }
    }

    private fun parseZip(filePath: Path, result: MutableList<RawLogRecord>) {
        val zip = ZipFile(filePath.absolutePathString())
        val names = zip.entries().toList().map { it.name }.sorted()
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
        var order = result.size
        for (line in lines) {
            val matches = LOG_REGEX.matchEntire(line)
            if (matches != null) {
                cache?.let(result::add)

                // TODO ну два раза гонять регулярку это прикол, да и не должен парсер теги трогать вот так
                val deobfuscatedTag = proguardInteractor?.deobfuscateClass(matches.groups[4]!!.value)
                if (deobfuscatedTag != null) {
                    val newLine = line.replaceRange(matches.groups[4]!!.range, deobfuscatedTag)
                    val matches = LOG_REGEX.matchEntire(newLine)!!
                    cache = RawLogRecord(
                        order = ++order,
                        raw = newLine,
                        time = matches.groups[1]!!.range,
                        timeInstant = OffsetDateTime.parse(
                            matches.groups[1]!!.value,
                            DATE_FORMATTER,
                        ).toInstant(),
                        thread = matches.groups[2]!!.range,
                        level = matches.groups[3]!!.range,
                        tag = matches.groups[4]!!.range,
                        message = matches.groups[5]!!.range,
                    )
                } else {
                    cache = RawLogRecord(
                        order = ++order,
                        raw = line,
                        time = matches.groups[1]!!.range,
                        timeInstant = OffsetDateTime.parse(
                            matches.groups[1]!!.value,
                            DATE_FORMATTER,
                        ).toInstant(),
                        thread = matches.groups[2]!!.range,
                        level = matches.groups[3]!!.range,
                        tag = matches.groups[4]!!.range,
                        message = matches.groups[5]!!.range,
                    )
                }
            } else {
                val oldCache = cache!!
                cache = oldCache.copy(
                    raw = oldCache.raw + "\n" + line,
                    message = IntRange(
                        start = oldCache.message.start,
                        endInclusive = oldCache.message.last + line.length + 1,
                    ),
                )
            }
        }
        result.add(cache!!)
    }

    // TODO чисто костыль чисто по бырику.
    private fun tryDeobfuscateStackTrace(message: String): String? {
        if (!message.contains("\n")) return null

        val lines = message.lines()
        var emptyCount = 0 // ??? пофиг, потом разберусь
        val stackCount = lines.dropLastWhile {
            if (it.isEmpty()) {
                emptyCount++
                true
            } else {
                false
            }
        }.takeLastWhile { it.startsWith("\tat ") }.count()
        if (stackCount == 0) return null

        for (l in lines.size - stackCount downTo 0) {
            val stackString = lines.subList(l, lines.size).joinToString(separator = "\n")
            StacktraceParser.parse(stackString)
                .onSuccess { stack ->
                    return message.replaceRange(
                        message.length - stackString.length,
                        message.length,
                        proguardInteractor!!.deobfuscateStack(stack).toString(),
                    )
                }
        }

        return null
    }

    companion object {
        @Suppress("MaxLineLength")
        private val LOG_REGEX = Regex(
            pattern = "^(\\d{4}-\\d{2}-\\d{2}T\\+\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) ([^ ]+) (.*)",
        )

        private val DATE_FORMATTER = DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendOffset("+HH:MM", "+00:00") // парсим смещение
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
