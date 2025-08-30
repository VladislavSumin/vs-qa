package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogParserLogger
import java.nio.file.Path
import kotlin.io.path.bufferedReader
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
            val lines = filePath.bufferedReader().lineSequence()
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
        LogParserLogger.d { "Parsed file $filePath at ${totalParseTime}ms. Lines = ${result.last().line}, logs = ${result.size}}" }
        return result
    }

    companion object {
        private val LOG_REGEX = Regex(
            pattern = "^(\\d{4}-\\d{2}-\\d{2}T\\+\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) ([^ ]+) ([A-Z]) (.*)",
        )
    }
}
