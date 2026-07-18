package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.substring
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class LiveProcessNameEnricherTest {

    init {
        LoggerManager.initTest()
    }

    private class FakeAdbClient : AdbClient {
        var processes: Map<Int, String> = emptyMap()
        var error: Throwable? = null
        var listProcessesCalls = 0

        override suspend fun listProcesses(deviceName: String): AdbClient.AdbResult<Map<Int, String>> {
            listProcessesCalls++
            val error = error
            return if (error != null) {
                AdbClient.AdbResult.Err(error)
            } else {
                AdbClient.AdbResult.Ok(processes)
            }
        }

        override fun observeDevices(): Flow<AdbClient.AdbResult<List<AdbClient.DeviceInfo>>> = error("not used")

        override suspend fun executeShellCommand(
            deviceName: String,
            shellCommand: String,
        ): AdbClient.AdbResult<String> = error("not used")

        override fun observeLogcat(
            deviceName: String,
            format: AdbClient.LogcatOutputFormat,
        ): Flow<AdbClient.AdbResult<String>> = error("not used")

        override fun observeBinaryLogcat(deviceName: String): Flow<AdbClient.AdbResult<ByteArray>> = error("not used")

        override suspend fun pullFile(
            deviceName: String,
            remotePath: String,
            localPath: String,
        ): AdbClient.AdbResult<Unit> = error("not used")
    }

    @Test
    fun `resolves and inserts process name`() = runTest {
        val adb = FakeAdbClient().apply { processes = mapOf(123 to "com.app:push") }
        val enricher = LiveProcessNameEnricher(adb, DEVICE)

        val result = enricher.enrich(flowOf(record("123"))).toList()

        assertEquals(1, result.size)
        assertEquals("com.app:push", result[0].raw.substring(result[0].processName!!))
        assertEquals("123", result[0].raw.substring(result[0].processId!!))
        assertEquals(1, adb.listProcessesCalls)
    }

    @Test
    fun `caches resolved names`() = runTest {
        val adb = FakeAdbClient().apply { processes = mapOf(123 to "com.app") }
        val enricher = LiveProcessNameEnricher(adb, DEVICE)

        val result = enricher.enrich(flowOf(record("123"), record("123"), record("123"))).toList()

        assertEquals(3, result.size)
        assertEquals(1, adb.listProcessesCalls)
        result.forEach { assertEquals("com.app", it.raw.substring(it.processName!!)) }
    }

    @Test
    fun `dead pid is negative cached`() = runTest {
        val timeSource = TestTimeSource()
        val adb = FakeAdbClient()
        val enricher = LiveProcessNameEnricher(adb, DEVICE, timeSource = timeSource)

        val records = flow {
            emit(record("999"))
            timeSource += 2.seconds
            emit(record("999"))
        }
        val result = enricher.enrich(records).toList()

        assertEquals(2, result.size)
        assertEquals(1, adb.listProcessesCalls)
        result.forEach { assertNull(it.processName) }
    }

    @Test
    fun `cooldown prevents repeated requests`() = runTest {
        val timeSource = TestTimeSource()
        val adb = FakeAdbClient()
        val enricher = LiveProcessNameEnricher(adb, DEVICE, timeSource = timeSource)

        val records = flow {
            emit(record("1"))
            emit(record("2"))
            adb.processes = mapOf(2 to "second.app")
            timeSource += 2.seconds
            emit(record("2"))
        }
        val result = enricher.enrich(records).toList()

        assertEquals(3, result.size)
        assertEquals(2, adb.listProcessesCalls)
        assertNull(result[0].processName)
        assertNull(result[1].processName)
        assertEquals("second.app", result[2].raw.substring(result[2].processName!!))
    }

    @Test
    fun `list processes error emits record unchanged`() = runTest {
        val adb = FakeAdbClient().apply { error = RuntimeException("adb died") }
        val enricher = LiveProcessNameEnricher(adb, DEVICE)

        val result = enricher.enrich(flowOf(record("123"))).toList()

        assertEquals(1, result.size)
        assertNull(result[0].processName)
        assertEquals(1, adb.listProcessesCalls)
    }

    @Test
    fun `record without pid passes through`() = runTest {
        val record = record("123").copy(processId = null)
        val adb = FakeAdbClient()
        val enricher = LiveProcessNameEnricher(adb, DEVICE)

        val result = enricher.enrich(flowOf(record)).toList()

        assertEquals(1, result.size)
        assertNull(result[0].processName)
        assertEquals(0, adb.listProcessesCalls)
    }

    private fun record(pid: String): RawLogRecord {
        val date = "01-02 10:20:30.123"
        val tid = "456"
        val tag = "Tag"
        val message = "message"
        val raw = "$date $pid:$tid I $tag $message"

        val dateRange = LogRange(0, date.length - 1)
        val pidRange = LogRange(dateRange.last + 2, dateRange.last + 1 + pid.length)
        val tidRange = LogRange(pidRange.last + 2, pidRange.last + 1 + tid.length)
        val levelRange = LogRange(tidRange.last + 2, tidRange.last + 2)
        val tagRange = LogRange(levelRange.last + 2, levelRange.last + 1 + tag.length)

        return RawLogRecord(
            raw = raw,
            time = dateRange,
            timeDate = LogRange(0, 0),
            timeInstant = Instant.parse("2026-01-02T10:20:30.123Z"),
            level = levelRange,
            logLevel = LogLevel.INFO,
            processId = pidRange,
            thread = tidRange,
            tag = tagRange,
            message = LogRange(tagRange.last + 2, raw.length - 1),
            lines = 1,
        )
    }

    private companion object {
        const val DEVICE = "emulator-5554"
    }
}
