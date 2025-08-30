package ru.vladislavsumin.qa.domain.logs

import java.nio.file.Path


interface LogsInteractor {
}

class LogsInteractorImpl(
    private val logPath: Path,
) : LogsInteractor {
    val logs = loadLogs()

    fun loadLogs(): List<RawLogRecord> {
        return AnimeLogParser().parseLog(logPath)
    }
}

