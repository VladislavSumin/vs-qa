package ru.vladislavsumin.feature.logViewer.domain.headless

import kotlinx.serialization.Serializable
import java.nio.file.Path

interface LogHeadlessProcessor {
    suspend fun process(logPath: Path, filterExpression: String, offset: Int = 0, limit: Int = 100): LogHeadlessResult
}

@Serializable
data class LogHeadlessResult(
    val total: Int,
    val filtered: Int,
    val offset: Int,
    val limit: Int,
    val records: List<LogHeadlessRecord>,
)

@Serializable
data class LogHeadlessRecord(
    val order: Int,
    val raw: String,
    val time: String,
    val level: String,
    val pid: String?,
    val tid: String?,
    val tag: String,
    val message: String,
)
