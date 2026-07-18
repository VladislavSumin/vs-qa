package ru.vladislavsumin.feature.logParser.logcat.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.StringFlowLogParser

/**
 * Парсит поток строк в logcat формате `-v long`, эмитя записи по мере поступления.
 */
class LogcatLongStringFlowLogParser : StringFlowLogParser {
    override fun parseLog(lines: Flow<String>): Flow<RawLogRecord> = flow {
        LogcatLogger.i { "Start parsing string flow with ${this@LogcatLongStringFlowLogParser.javaClass.simpleName}" }
        val session = LogcatLongLogParser.Session()
        lines.collect { line ->
            session.onLine(line)?.let { emit(it) }
        }
        session.finish()?.let { emit(it) }
    }
}
