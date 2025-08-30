package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogParserLogger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

interface LogParser {
    fun parseLog(filePath: Path): List<RawLogRecord>
}

class AnimeLogParser() : LogParser {
    override fun parseLog(filePath: Path): List<RawLogRecord> {
        LogParserLogger.i { "Start parsing file $filePath with ${this.javaClass.simpleName}" }
        val result = mutableListOf<RawLogRecord>()
        val totalParseTime = measureTimeMillis {
            val lines: List<String>
            val readFileTime = measureTimeMillis {
                lines = Files.readAllLines(filePath)
            }
            LogParserLogger.d { "Read file with ${lines.size} lines at ${readFileTime}ms" }
            val parseLogTime = measureTimeMillis {
                var cache: RawLogRecord? = null
                var order = 0
                for ((index, line) in lines.withIndex()) {
                    val matches = LOG_REGEX.matchEntire(line)
                    if (matches != null) {
                        if (cache != null) {
                            result.add(cache)
                        }
                        cache = RawLogRecord(
                            line = index,
                            order = ++order,
                            raw = line,
                            time = matches.groupValues[0],
                            thread = matches.groupValues[1],
                            level = matches.groupValues[2],
                            tag = matches.groupValues[3],
                            message = matches.groupValues[4],
                        )
                    } else {
                        val oldCache = cache!!
                        cache = oldCache.copy(
                            raw = oldCache.raw + "\n" + line,
                            message = oldCache.message + "\n" + line
                        )
                    }
                }
                result.add(cache!!)
            }
            LogParserLogger.d { "Parsed ${result.size} logs at ${parseLogTime}ms" }
        }
        LogParserLogger.d { "Parsed file $filePath at ${totalParseTime}ms" }
        return result
    }

    companion object {
        private val LOG_REGEX =
            Regex("^(\\d{4}-\\d{2}-\\d{2}T\\+\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) (.*)")

    }
}