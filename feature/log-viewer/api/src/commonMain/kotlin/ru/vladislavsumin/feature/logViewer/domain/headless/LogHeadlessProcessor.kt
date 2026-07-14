package ru.vladislavsumin.feature.logViewer.domain.headless

import kotlinx.serialization.Serializable
import java.nio.file.Path

interface LogHeadlessProcessor {
    suspend fun process(logPath: Path, filterExpression: String, offset: Int = 0, limit: Int = 100): LogHeadlessResult

    suspend fun parseAndCache(logPath: Path): Int

    suspend fun query(filterExpression: String, offset: Int = 0, limit: Int = 100): LogHeadlessResult

    fun release()
}

@Serializable
data class LogHeadlessResult(
    val total: Int,
    val filtered: Int,
    val offset: Int,
    val limit: Int,
    val records: List<LogHeadlessRecord>,
    val error: LogHeadlessError? = null,
)

@Serializable
data class LogHeadlessError(val type: String, val message: String)

@Serializable
data class LogHeadlessRecord(
    val order: Int,
    val time: String,
    val level: String,
    val pid: String? = null,
    val tid: String? = null,
    val tag: String,
    val message: String,
)
