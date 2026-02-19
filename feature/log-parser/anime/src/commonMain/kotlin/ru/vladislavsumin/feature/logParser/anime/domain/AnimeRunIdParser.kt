package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.runId.RawRunIdInfo
import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser

internal class AnimeRunIdParser : RunIdParser {
    override suspend fun provideRunIdMeta(logs: List<RawLogRecord>): List<RawRunIdInfo>? {
        if (logs.isEmpty()) return null

        if (logs.first().processId != null) {
            // For logcat logs
            // К сожалению в данный момент в таких логах недостаточно информации для построения информации о запусках.
            return null
        }

        // For embedded logs
        val indexes = mutableListOf<RawRunIdInfo>()
        var prevPid = -1
        logs.forEachIndexed { index, record ->
            if (
                record.raw.substring(record.tag) == "OneMeFileLogger" &&
                record.raw.substring(record.message).startsWith("AppInfo:")
            ) {
                val data = record.raw.substring(record.message).lines()
                    .drop(1) // Drop AppInfo header
                    .filter { it.isNotBlank() }
                    .associate {
                        val (k, v) = it.split(":", limit = 2)
                        k.trim() to v.trim()
                    }
                val version = data["AppVersion"]!!
                val pid = data["PID"]?.toIntOrNull()
                if (pid == null || pid != prevPid) {
                    prevPid = pid ?: -1
                    indexes.add(RawRunIdInfo(index, "version" to version))
                } else {
                    AnimeLogger.d { "Find more then one log start marker for $pid, concatenate it" }
                }
            }
        }
        return if (indexes.isEmpty()) null else indexes
    }
}
