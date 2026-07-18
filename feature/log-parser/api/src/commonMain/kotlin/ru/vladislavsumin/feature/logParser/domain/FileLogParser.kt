package ru.vladislavsumin.feature.logParser.domain

import java.nio.file.Path

interface FileLogParser {
    suspend fun parseLog(filePath: Path): List<RawLogRecord>
}
