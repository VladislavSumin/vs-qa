package ru.vladislavsumin.feature.logParser.domain

import kotlinx.coroutines.flow.Flow

interface StringFlowLogParser {
    fun parseLog(lines: Flow<String>): Flow<RawLogRecord>
}
