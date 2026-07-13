package ru.vladislavsumin.feature.logViewer.domain.headless

import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.substring
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.toLogRecords
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterRequestParser
import java.nio.file.Path

internal class LogHeadlessProcessorImpl(private val logParserProvider: LogParserProvider) : LogHeadlessProcessor {

    private val filterParser = FilterRequestParser(MutableStateFlow(emptyList()))

    override suspend fun process(logPath: Path, filterExpression: String, offset: Int, limit: Int): LogHeadlessResult {
        val rawLogs = runCatching {
            logParserProvider.getLogParser().parseLog(logPath)
        }.getOrElse {
            return LogHeadlessResult(
                total = 0,
                filtered = 0,
                offset = offset,
                limit = limit,
                records = emptyList(),
                error = LogHeadlessError(type = "file_error", message = it.message ?: "Failed to read log file"),
            )
        }

        val logRecords = rawLogs.toLogRecords()
        val filterResult = applyFilter(logRecords, filterExpression, offset, limit)
        if (filterResult != null) return filterResult

        return buildResult(logRecords, logRecords, offset, limit)
    }

    private fun applyFilter(
        logRecords: List<LogRecord>,
        expression: String,
        offset: Int,
        limit: Int,
    ): LogHeadlessResult? {
        if (expression.isBlank()) return null

        val filterRequest = filterParser.parse(expression).searchRequest.getOrElse {
            return LogHeadlessResult(
                total = logRecords.size,
                filtered = 0,
                offset = offset,
                limit = limit,
                records = emptyList(),
                error = LogHeadlessError(
                    type = "filter_parse_error",
                    message = it.message ?: "Invalid filter expression",
                ),
            )
        }

        val prepared = filterRequest.operation.prepare(null)
        if (prepared == null) return null

        val filtered = logRecords.parallelStream()
            .filter { prepared.check(it) }
            .toList()
        return buildResult(logRecords, filtered, offset, limit)
    }

    private fun buildResult(
        logRecords: List<LogRecord>,
        filtered: List<LogRecord>,
        offset: Int,
        limit: Int,
    ): LogHeadlessResult {
        val paged = filtered.drop(offset).take(limit)
        val records = paged.map { log ->
            LogHeadlessRecord(
                order = log.order.value,
                raw = log.raw,
                time = log.raw.substring(log.time),
                level = log.logLevel.name,
                pid = log.processId?.let { log.raw.substring(it) },
                tid = null,
                tag = log.raw.substring(log.tag),
                message = log.raw.substring(log.message),
            )
        }
        return LogHeadlessResult(
            total = logRecords.size,
            filtered = filtered.size,
            offset = offset,
            limit = limit,
            records = records,
        )
    }
}
