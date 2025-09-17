package ru.vladislavsumin.feature.logParser.domain

import java.nio.file.Path

interface LogParser {
    suspend fun parseLog(filePath: Path): List<RawLogRecord>
}
