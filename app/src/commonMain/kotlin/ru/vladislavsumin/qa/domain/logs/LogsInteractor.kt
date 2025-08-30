package ru.vladislavsumin.qa.domain.logs

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import ru.vladislavsumin.qa.LogLogger
import ru.vladislavsumin.qa.utils.measureTimeMillisWithResult
import java.nio.file.Path


interface LogsInteractor {
    suspend fun filterLogs(filter: String): List<RawLogRecord>
}

class LogsInteractorImpl(
    private val logPath: Path,
) : LogsInteractor {
    val logs = loadLogs()

    fun loadLogs(): List<RawLogRecord> {
        return AnimeLogParser().parseLog(logPath)
    }

    override suspend fun filterLogs(filter: String): List<RawLogRecord> {
        // На таком простом фильтре наивный многопоточный фильтр дает выигрыш порядка 40%
        // Производительность фильтра порядка 8кк записаей в секунду
        val (time, result) = measureTimeMillisWithResult { logs.filter { it.raw.contains(filter) } }
        LogLogger.d { "Log filtered at ${time}ms, size = ${result.size}" }
        return result
    }
}
