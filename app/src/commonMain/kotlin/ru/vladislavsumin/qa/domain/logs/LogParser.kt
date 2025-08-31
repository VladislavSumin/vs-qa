package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogParserLogger
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.extension
import kotlin.system.measureTimeMillis

interface LogParser {
    fun parseLog(filePath: Path): List<RawLogRecord>
}

class AnimeLogParser() : LogParser {
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
        LogParserLogger.d { "Parsed file $filePath at ${totalParseTime}ms. Lines = ${result.last().line}, logs = ${result.size}}" }
        return result
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

    private fun parseLines(lines: Sequence<String>, result: MutableList<RawLogRecord>) {
        var cache: RawLogRecord? = null
        var order = 0
        for ((index, line) in lines.withIndex()) {
            val matches = LOG_REGEX.matchEntire(line)
            if (matches != null) {
                cache?.let(result::add)
                cache = RawLogRecord(
                    line = index,
                    order = ++order,
                    raw = line,
                    time = matches.groups[1]!!.range,
                    thread = matches.groups[2]!!.range,
                    level = matches.groups[3]!!.range,
                    tag = matches.groups[4]!!.range,
                    message = matches.groups[5]!!.range,
                )
            } else {
                val oldCache = cache!!
                cache = oldCache.copy(
                    raw = oldCache.raw + "\n" + line,
                    message = IntRange(
                        start = oldCache.message.start,
                        endInclusive = oldCache.message.last + line.length + 1,
                    )
                )
            }
        }
        result.add(cache!!)
    }

    companion object {
        private val LOG_REGEX = Regex(
            pattern = "^(\\d{4}-\\d{2}-\\d{2}T\\+\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) ([^ ]+) (.*)",
        )
    }
}
