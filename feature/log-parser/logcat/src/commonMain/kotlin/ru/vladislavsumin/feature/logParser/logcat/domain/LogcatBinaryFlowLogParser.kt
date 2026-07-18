package ru.vladislavsumin.feature.logParser.logcat.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.vladislavsumin.feature.logParser.domain.BinaryFlowLogParser
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord

/**
 * Парсит поток бинарных данных в logcat формате (`logcat --binary`), эмитя записи по мере поступления.
 */
class LogcatBinaryFlowLogParser : BinaryFlowLogParser {
    override fun parseLog(data: Flow<ByteArray>): Flow<RawLogRecord> = flow {
        LogcatLogger.i { "Start parsing binary flow with ${this@LogcatBinaryFlowLogParser.javaClass.simpleName}" }
        val session = LogcatBinaryLogParser.Session()
        data.collect { chunk ->
            session.onChunk(chunk).forEach { emit(it) }
        }
    }
}
