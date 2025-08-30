package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogLogger
import ru.vladislavsumin.qa.utils.measureTimeMillisWithResult
import java.nio.file.Path


interface LogsInteractor {
    suspend fun filterLogs(filter: String): List<RawLogRecord>
    suspend fun filterLogs(filter: Regex): List<RawLogRecord>
}

class LogsInteractorImpl(
    private val logPath: Path,
) : LogsInteractor {
    val logs = loadLogs()

    fun loadLogs(): List<RawLogRecord> {
        return AnimeLogParser().parseLog(logPath)
    }

    override suspend fun filterLogs(filter: String): List<RawLogRecord> = filterLogs { it.raw.contains(filter) }
    override suspend fun filterLogs(filter: Regex): List<RawLogRecord> = filterLogs { it.raw.matches(filter) }

    private fun filterLogs(filter: (RawLogRecord) -> Boolean): List<RawLogRecord> {
        val (time, result) = measureTimeMillisWithResult {
            logs.parallelStream()
                .filter(filter)
                .toList()
        }
        LogLogger.d { "Log filtered at ${time}ms, size = ${result.size}" }
        return result
    }
}
