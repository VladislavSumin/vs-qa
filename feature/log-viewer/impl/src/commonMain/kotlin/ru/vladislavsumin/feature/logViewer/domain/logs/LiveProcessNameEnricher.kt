package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.substring
import ru.vladislavsumin.feature.logViewer.LogLogger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Обогащает live поток логов именем процесса (`pid -> имя` через `ps` на устройстве).
 *
 * Имена кешируются, для неизвестных pid выполняется батч запрос списка процессов,
 * но не чаще чем раз в [cooldown]. Записи с pid, которые не удалось определить в момент
 * обработки (мертвый процесс или активный cooldown), эмитятся без имени.
 */
internal class LiveProcessNameEnricher(
    private val adbClient: AdbClient,
    private val deviceName: String,
    private val cooldown: Duration = 1.seconds,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private val cache = mutableMapOf<String, String?>()
    private var lastRequestMark: TimeMark? = null

    fun enrich(records: Flow<RawLogRecord>): Flow<RawLogRecord> = records.map { record ->
        val pidRange = record.processId ?: return@map record
        val pid = record.raw.substring(pidRange)
        if (pid !in cache) {
            tryResolve(pid)
        }
        val name = cache[pid]
        if (name != null) record.withProcessName(name) else record
    }

    private suspend fun tryResolve(pid: String) {
        val mark = lastRequestMark
        if (mark != null && mark.elapsedNow() < cooldown) return
        lastRequestMark = timeSource.markNow()

        when (val result = adbClient.listProcesses(deviceName)) {
            is AdbClient.AdbResult.Err -> LogLogger.e(result.t) { "Failed to list processes for $deviceName" }

            is AdbClient.AdbResult.Ok -> {
                result.data.forEach { (processId, name) -> cache[processId.toString()] = name }
                if (pid !in cache) {
                    // Процесс уже мертв, запоминаем что бы не запрашивать повторно.
                    cache[pid] = null
                }
            }
        }
    }
}
