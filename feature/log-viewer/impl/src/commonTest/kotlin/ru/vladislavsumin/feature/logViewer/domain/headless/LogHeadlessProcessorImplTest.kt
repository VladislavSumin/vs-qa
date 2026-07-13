package ru.vladislavsumin.feature.logViewer.domain.headless

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.feature.logParser.anime.domain.AnimeLogParserProvider
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("MaximumLineLength", "MaxLineLength")
class LogHeadlessProcessorImplTest {

    private val processor = LogHeadlessProcessorImpl(AnimeLogParserProvider())
    private val logPath = resolveTestLogPath()

    private fun resolveTestLogPath(): java.nio.file.Path {
        var dir: java.nio.file.Path = Path("").toAbsolutePath()
        while (true) {
            val candidate = dir.resolve("test-data/sample-anime.log")
            if (candidate.toFile().exists()) return candidate
            dir = dir.parent ?: error("Cannot find test-data/sample-anime.log from working directory")
        }
    }

    @BeforeTest
    fun initLogger() {
        synchronized(TestLoggerInit) {
            if (!TestLoggerInit.initialized) {
                TestLoggerInit.initialized = true
                try {
                    LoggerManager.init(externalLoggerFactory = {
                        object : ExternalLogger {
                            override fun log(level: LogLevel, msg: String) = Unit
                            override fun log(level: LogLevel, throwable: Throwable, msg: String) = Unit
                        }
                    })
                } catch (_: IllegalStateException) {
                    // Already initialized by another test class
                }
            }
        }
    }

    private object TestLoggerInit {
        var initialized = false
    }

    @Test
    fun `all records without filter`() {
        val result = process(logPath, "")
        assertEquals(160, result.total)
        assertEquals(160, result.filtered)
        assertNull(result.error)
    }

    @Test
    fun `filter by level ERROR`() {
        val result = process(logPath, "level=ERROR")
        assertNull(result.error)
        assertEquals(12, result.filtered)
        assertTrue(result.records.all { it.level == "ERROR" || it.level == "FATAL" })
    }

    @Test
    fun `filter by level WARN`() {
        val result = process(logPath, "level=WARN")
        assertNull(result.error)
        assertEquals(29, result.filtered)
        assertTrue(result.records.all { it.level in setOf("WARN", "ERROR", "FATAL") })
    }

    @Test
    fun `filter by level INFO`() {
        val result = process(logPath, "level=INFO")
        assertNull(result.error)
        assertEquals(102, result.filtered)
        assertTrue(result.records.all { it.level in setOf("INFO", "WARN", "ERROR", "FATAL") })
    }

    @Test
    fun `filter by tag`() {
        val result = process(logPath, "tag=BluetoothManager")
        assertNull(result.error)
        assertEquals(13, result.filtered)
        assertTrue(result.records.all { it.tag == "BluetoothManager" })
    }

    @Test
    fun `filter by tag AND level`() {
        val result = process(logPath, "tag=PaymentGateway & level=ERROR")
        assertNull(result.error)
        assertEquals(4, result.filtered)
        assertTrue(
            result.records.all {
                it.tag == "PaymentGateway" && it.level in setOf("ERROR", "FATAL")
            },
        )
    }

    @Test
    fun `filter by message contains`() {
        val result = process(logPath, "message=timeout")
        assertNull(result.error)
        assertEquals(1, result.filtered)
        assertTrue(result.records.all { it.message.contains("timeout", ignoreCase = true) })
    }

    @Test
    fun `filter by bare text search`() {
        val result = process(logPath, "BluetoothManager")
        assertNull(result.error)
        assertEquals(14, result.filtered)
    }

    @Test
    fun `filter with OR logic`() {
        val result = process(logPath, "tag=CrashHandler | tag=PaymentGateway")
        assertNull(result.error)
        assertTrue(result.filtered > 0)
        assertTrue(result.records.all { it.tag == "CrashHandler" || it.tag == "PaymentGateway" })
    }

    @Test
    fun `filter with NOT logic`() {
        val result = process(logPath, "!tag=App", limit = 200)
        assertNull(result.error)
        assertTrue(result.filtered < result.total)
        assertTrue(result.records.all { it.tag != "App" })
    }

    @Test
    fun `filter with brackets`() {
        val result = process(logPath, "(tag=CrashHandler | tag=PaymentGateway) & level=ERROR")
        assertNull(result.error)
        assertTrue(result.filtered > 0)
        assertTrue(
            result.records.all {
                (it.tag == "CrashHandler" || it.tag == "PaymentGateway") && it.level == "ERROR"
            },
        )
    }

    @Test
    fun `filter by run number returns all without runIdIndexes`() {
        val result = process(logPath, "runNumber=1")
        assertNull(result.error)
        assertEquals(160, result.filtered)
    }

    @Test
    fun `run number 2 returns all without runIdIndexes`() {
        val result = process(logPath, "runNumber=2")
        assertNull(result.error)
        assertEquals(160, result.filtered)
    }

    @Test
    fun `run filters require runIdIndexes`() {
        val result1 = process(logPath, "runNumber=1")
        val result2 = process(logPath, "runNumber=2")
        assertNull(result1.error)
        assertNull(result2.error)
        assertEquals(result1.filtered, result2.filtered)
    }

    @Test
    fun `default offset and limit`() {
        val result = process(logPath, "", offset = 0, limit = 100)
        assertNull(result.error)
        assertEquals(160, result.filtered)
        assertEquals(100, result.records.size)
        assertEquals(0, result.offset)
        assertEquals(100, result.limit)
    }

    @Test
    fun `limit truncates records`() {
        val result = process(logPath, "", offset = 0, limit = 5)
        assertNull(result.error)
        assertEquals(5, result.records.size)
    }

    @Test
    fun `offset skips records`() {
        val full = process(logPath, "", offset = 0, limit = 160)
        val paged = process(logPath, "", offset = 5, limit = 5)
        assertNull(full.error)
        assertNull(paged.error)
        assertEquals(5, paged.records.size)
        assertEquals(full.records[5].order, paged.records[0].order)
        assertEquals(full.records[5].message, paged.records[0].message)
    }

    @Test
    fun `offset beyond filtered returns empty`() {
        val result = process(logPath, "", offset = 200, limit = 10)
        assertNull(result.error)
        assertEquals(0, result.records.size)
    }

    @Test
    fun `empty filter string is same as no filter`() {
        val empty = process(logPath, "")
        val noFilter = process(logPath, "   ")
        assertEquals(empty.total, noFilter.total)
        assertEquals(empty.filtered, noFilter.filtered)
    }

    @Test
    fun `invalid filter expression returns error`() {
        val result = process(logPath, "level>=ERROR")
        assertNotNull(result.error)
        assertEquals("filter_parse_error", result.error!!.type)
        assertTrue(result.error!!.message.contains("Could not parse input", ignoreCase = true))
        assertEquals(0, result.filtered)
        assertEquals(0, result.records.size)
    }

    @Test
    fun `unknown field is treated as bare text filter`() {
        val result = process(logPath, "unknownField=value")
        assertNull(result.error)
        assertEquals(0, result.filtered)
    }

    @Test
    fun `non-existent file returns error`() {
        val result = process(Path("/nonexistent/path/log.log"), "")
        assertNotNull(result.error)
        assertEquals("file_error", result.error!!.type)
        assertEquals(0, result.total)
        assertEquals(0, result.records.size)
    }

    @Test
    fun `record fields are correctly mapped`() {
        val result = process(logPath, "", offset = 0, limit = 1)
        assertNull(result.error)
        val record = result.records[0]
        assertEquals(0, record.order)
        assertEquals("INFO", record.level)
        assertEquals("App", record.tag)
        assertTrue(record.message.contains("AppInfo"))
        assertTrue(record.raw.isNotEmpty())
        assertTrue(record.time.isNotEmpty())
    }

    @Test
    fun `multi-line stacktrace is included in message`() {
        val result = process(logPath, "tag=NetworkMonitor & message=SSL")
        assertNull(result.error)
        assertEquals(1, result.filtered)
        val record = result.records[0]
        assertTrue(record.message.contains("SSLHandshakeException"))
        assertTrue(record.message.contains("Certificate expired"))
        assertTrue(record.message.lines().size > 3)
    }

    @Test
    fun `crash handler error has complete stacktrace`() {
        val result = process(logPath, "tag=CrashHandler & level=ERROR")
        assertNull(result.error)
        assertEquals(2, result.filtered)
        val record = result.records.first { it.message.contains("uncaught exception") }
        assertEquals("CrashHandler", record.tag)
        assertEquals("ERROR", record.level)
        assertTrue(record.message.contains("SecurityException"))
        assertTrue(record.message.contains("BLUETOOTH_CONNECT"))
        assertTrue(record.message.lines().size > 5)
    }

    @Test
    fun `result is json serializable`() {
        val result = process(logPath, "", offset = 0, limit = 1)
        assertNull(result.error)
        val json = kotlinx.serialization.json.Json.encodeToString(result)
        assertTrue(json.contains("\"total\""))
        assertTrue(json.contains("\"records\""))
    }

    @Test
    fun `error result is json serializable`() {
        val result = process(Path("/nonexistent/path/log.log"), "")
        assertNotNull(result.error)
        val json = kotlinx.serialization.json.Json.encodeToString(result)
        assertTrue(json.contains("\"error\""))
        assertTrue(json.contains("file_error"))
    }

    @Test
    fun `inline log parsing works`() {
        val tempFile = Files.createTempFile("test-log-", ".log")
        try {
            tempFile.writeText(
                buildString {
                    appendLine("2024-01-01T+00:00 10:00:00.000 main I Alpha first")
                    appendLine("2024-01-01T+00:00 10:00:01.000 main E Alpha second")
                    appendLine("2024-01-01T+00:00 10:00:02.000 main W Beta third")
                },
            )

            val result = process(tempFile, "level=ERROR")
            assertNull(result.error)
            assertEquals(3, result.total)
            assertEquals(1, result.filtered)
            assertEquals("Alpha", result.records[0].tag)
            assertEquals("ERROR", result.records[0].level)
        } finally {
            tempFile.toFile().delete()
        }
    }

    @Test
    fun `all log levels are present in test data`() {
        val result = process(logPath, "", offset = 0, limit = 200)
        assertNull(result.error)
        val levels = result.records.map { it.level }.toSet()
        assertTrue(levels.contains("VERBOSE"))
        assertTrue(levels.contains("DEBUG"))
        assertTrue(levels.contains("INFO"))
        assertTrue(levels.contains("WARN"))
        assertTrue(levels.contains("ERROR"))
        assertTrue(levels.contains("FATAL"))
    }

    private fun process(
        path: java.nio.file.Path,
        filter: String,
        offset: Int = 0,
        limit: Int = 100,
    ): LogHeadlessResult = run {
        kotlinx.coroutines.runBlocking {
            processor.process(path, filter, offset, limit)
        }
    }
}
