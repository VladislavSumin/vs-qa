package ru.vladislavsumin.feature.logViewer.domain.headless

import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.substring
import ru.vladislavsumin.feature.logViewer.domain.logs.toLogRecords
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterRequestParser
import java.nio.file.Path

internal class LogHeadlessProcessorImpl(private val logParserProvider: LogParserProvider) : LogHeadlessProcessor {

    private val filterParser = FilterRequestParser(MutableStateFlow(emptyList()))

    override suspend fun process(logPath: Path, filterExpression: String, offset: Int, limit: Int): LogHeadlessResult {
        val rawLogs = logParserProvider.getLogParser().parseLog(logPath)
        val logRecords = rawLogs.toLogRecords()

        val filteredLogs = if (filterExpression.isBlank()) {
            logRecords
        } else {
            val filterRequest = filterParser.parse(filterExpression)
                .searchRequest
                .getOrThrow()

            val prepared = filterRequest.operation.prepare(null)
            if (prepared != null) {
                logRecords.parallelStream()
                    .filter { prepared.check(it) }
                    .toList()
            } else {
                logRecords
            }
        }

        val total = logRecords.size
        val paged = filteredLogs.drop(offset).take(limit)

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
            total = total,
            filtered = filteredLogs.size,
            offset = offset,
            limit = limit,
            records = records,
        )
    }
}
