package ru.vladislavsumin.feature.logParser.domain

import ru.vladislavsumin.feature.logParser.domain.runId.RawRunIdInfo
import java.nio.file.Path

interface FileLogParser {
    suspend fun parseLog(filePath: Path): ParseResult

    data class ParseResult(val records: List<RawLogRecord>, val runIdInfo: List<RawRunIdInfo>?,)
}
