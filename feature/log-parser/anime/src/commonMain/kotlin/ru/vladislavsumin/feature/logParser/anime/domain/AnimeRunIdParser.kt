package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.runId.RawRunIdInfo
import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser

internal class AnimeRunIdParser : RunIdParser {
    override suspend fun provideRunIdMeta(logs: List<RawLogRecord>): List<RawRunIdInfo>? {
        val indexes = mutableListOf<RawRunIdInfo>()
        logs.forEachIndexed { index, record ->
            if (
                record.raw.substring(record.tag) == "OneMeFileLogger" &&
                record.raw.substring(record.message).startsWith("AppInfo:")
            ) {
                indexes.add(RawRunIdInfo(index))
            }
        }
        return if (indexes.isEmpty()) null else indexes
    }
}
