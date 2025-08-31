package ru.vladislavsumin.qa.domain.logs

import ru.vladislavsumin.qa.LogLogger
import ru.vladislavsumin.qa.utils.measureTimeMillisWithResult
import java.nio.file.Path


interface LogsInteractor {
    suspend fun filterAndSearchLogs(
        filter: String,
        search: String,
    ): List<RawLogRecord>

}

class LogsInteractorImpl(
    private val logPath: Path,
) : LogsInteractor {
    val logs = loadLogs()

    fun loadLogs(): List<RawLogRecord> {
        return AnimeLogParser().parseLog(logPath)
    }

    override suspend fun filterAndSearchLogs(filter: String, search: String): List<RawLogRecord> =
        filterLogs { it.raw.contains(filter) }
            .searchLogs {
                val index = it.raw.indexOfAny(listOf(search))
                if (index >= 0) {
                    IntRange(index, index + search.length - 1)
                } else {
                    null
                }
            }

    private fun filterLogs(filter: (RawLogRecord) -> Boolean): List<RawLogRecord> {
        val (time, result) = measureTimeMillisWithResult {
            logs.parallelStream()
                .filter(filter)
                .toList()
        }
        LogLogger.d { "Log filtered at ${time}ms, size = ${result.size}" }
        return result
    }

    private fun List<RawLogRecord>.searchLogs(search: (RawLogRecord) -> IntRange?): List<RawLogRecord> {
        val (time, result) = measureTimeMillisWithResult {
            this.parallelStream()
                .map { log -> search(log)?.let { log.copy(searchHighlight = it) } ?: log }
                .toList()
        }
        LogLogger.d { "Log searched at ${time}ms, size = ${result.size}" }
        return result
    }
}
