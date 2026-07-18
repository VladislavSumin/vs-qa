package ru.vladislavsumin.feature.logParser.domain

import kotlinx.coroutines.flow.Flow

interface BinaryFlowLogParser {
    fun parseLog(data: Flow<ByteArray>): Flow<RawLogRecord>
}
