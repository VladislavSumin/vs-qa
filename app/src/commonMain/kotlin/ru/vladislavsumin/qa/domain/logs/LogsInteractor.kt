package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogLogger
import ru.vladislavsumin.qa.utils.measureTimeMillisWithResult
import java.nio.file.Path


interface LogsInteractor {
    fun filterLogs(filter: String): List<RawLogRecord>
}

class LogsInteractorImpl(
    private val logPath: Path,
) : LogsInteractor {
    val logs = loadLogs()

    fun loadLogs(): List<RawLogRecord> {
        return AnimeLogParser().parseLog(logPath)
    }

    override fun filterLogs(filter: String): List<RawLogRecord> {
        val (time, result) = measureTimeMillisWithResult { logs.filter { it.raw.contains(filter) } }
        LogLogger.d { "Log filtered at ${time}ms, size = ${result.size}" }
        return result
    }
}
