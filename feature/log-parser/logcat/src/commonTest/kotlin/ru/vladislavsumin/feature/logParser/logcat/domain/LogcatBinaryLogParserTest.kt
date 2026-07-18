package ru.vladislavsumin.feature.logParser.logcat.domain

import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.substring
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogcatBinaryLogParserTest {

    init {
        LoggerManager.initTest()
    }

    @Test
    fun `single entry all fields`() {
        val records = parse(
            binaryLogcatEntry(
                pid = 12345,
                tid = 67890,
                sec = 1_700_000_000,
                nsec = 123_000_000,
                prio = 3,
                tag = "MyTag",
                message = "hello world",
            ),
        )

        assertEquals(1, records.size)
        val record = records[0]
        assertEquals("12345", record.raw.substring(record.processId!!))
        assertEquals("67890", record.raw.substring(record.thread))
        assertEquals("D", record.raw.substring(record.level))
        assertEquals(LogLevel.DEBUG, record.logLevel)
        assertEquals("MyTag", record.raw.substring(record.tag))
        assertEquals("hello world", record.raw.substring(record.message))
        assertEquals(Instant.ofEpochSecond(1_700_000_000, 123_000_000), record.timeInstant)
        assertTrue(
            record.raw.substring(record.time).matches(TIME_REGEX),
            "Unexpected time format: ${record.raw.substring(record.time)}",
        )
        assertEquals(1, record.lines)
    }

    @Test
    fun `multiple entries in one chunk`() {
        val records = parse(
            binaryLogcatEntry(tag = "First") +
                binaryLogcatEntry(tag = "Second") +
                binaryLogcatEntry(tag = "Third"),
        )

        assertEquals(3, records.size)
        assertEquals("First", records[0].raw.substring(records[0].tag))
        assertEquals("Second", records[1].raw.substring(records[1].tag))
        assertEquals("Third", records[2].raw.substring(records[2].tag))
    }

    @Test
    fun `entry split inside prefix`() {
        val full = binaryLogcatEntry(tag = "Split")
        val session = LogcatBinaryLogParser.Session()

        assertTrue(session.onChunk(full.copyOfRange(0, 2)).isEmpty())
        val records = session.onChunk(full.copyOfRange(2, full.size))

        assertEquals(1, records.size)
        assertEquals("Split", records[0].raw.substring(records[0].tag))
    }

    @Test
    fun `entry split inside header`() {
        val full = binaryLogcatEntry(tag = "Split")
        val session = LogcatBinaryLogParser.Session()

        assertTrue(session.onChunk(full.copyOfRange(0, 10)).isEmpty())
        val records = session.onChunk(full.copyOfRange(10, full.size))

        assertEquals(1, records.size)
    }

    @Test
    fun `entry split inside payload`() {
        val full = binaryLogcatEntry(tag = "Split", message = "long enough message")
        val session = LogcatBinaryLogParser.Session()

        assertTrue(session.onChunk(full.copyOfRange(0, 32)).isEmpty())
        val records = session.onChunk(full.copyOfRange(32, full.size))

        assertEquals(1, records.size)
        assertEquals("long enough message", records[0].raw.substring(records[0].message))
    }

    @Test
    fun `byte by byte feeding`() {
        val session = LogcatBinaryLogParser.Session()
        val data = binaryLogcatEntry(tag = "First") + binaryLogcatEntry(tag = "Second")

        val records = mutableListOf<RawLogRecord>()
        for (byte in data) {
            records += session.onChunk(byteArrayOf(byte))
        }

        assertEquals(2, records.size)
        assertEquals("First", records[0].raw.substring(records[0].tag))
        assertEquals("Second", records[1].raw.substring(records[1].tag))
    }

    @Test
    fun `multiline message`() {
        val records = parse(binaryLogcatEntry(message = "line one\nline two"))

        assertEquals(1, records.size)
        val record = records[0]
        assertEquals(2, record.lines)
        assertEquals(record.raw.lines().size, record.lines)
        assertEquals("line one\nline two", record.raw.substring(record.message))
    }

    @Test
    fun `trailing newlines and zeros stripped from message`() {
        val records = parse(binaryLogcatEntry(message = "msg\n\n"))

        assertEquals(1, records.size)
        assertEquals("msg", records[0].raw.substring(records[0].message))
        assertEquals(1, records[0].lines)
    }

    @Test
    fun `binary payload buffers are skipped`() {
        val records = parse(
            binaryLogcatEntry(lid = 2, tag = "Events") +
                binaryLogcatEntry(lid = 5, tag = "Stats") +
                binaryLogcatEntry(lid = 6, tag = "Security") +
                binaryLogcatEntry(lid = 4, tag = "Crash") +
                binaryLogcatEntry(lid = 0, tag = "Main"),
        )

        assertEquals(2, records.size)
        assertEquals("Crash", records[0].raw.substring(records[0].tag))
        assertEquals("Main", records[1].raw.substring(records[1].tag))
    }

    @Test
    fun `v1 and v3 header sizes supported`() {
        val records = parse(
            binaryLogcatEntry(hdrSize = 20, tag = "V1") +
                binaryLogcatEntry(hdrSize = 24, tag = "V3") +
                binaryLogcatEntry(hdrSize = 24, lid = 2, tag = "V3Events"),
        )

        assertEquals(2, records.size)
        assertEquals("V1", records[0].raw.substring(records[0].tag))
        assertEquals("V3", records[1].raw.substring(records[1].tag))
    }

    @Test
    fun `all priorities mapping`() {
        val records = parse(
            binaryLogcatEntry(prio = 2) +
                binaryLogcatEntry(prio = 3) +
                binaryLogcatEntry(prio = 4) +
                binaryLogcatEntry(prio = 5) +
                binaryLogcatEntry(prio = 6) +
                binaryLogcatEntry(prio = 7),
        )

        assertEquals(
            listOf(
                LogLevel.VERBOSE,
                LogLevel.DEBUG,
                LogLevel.INFO,
                LogLevel.WARN,
                LogLevel.ERROR,
                LogLevel.FATAL,
            ),
            records.map { it.logLevel },
        )
        assertEquals(
            listOf("V", "D", "I", "W", "E", "F"),
            records.map { it.raw.substring(it.level) },
        )
    }

    @Test
    fun `unknown priority is skipped`() {
        val records = parse(
            binaryLogcatEntry(prio = 0, tag = "Unknown") +
                binaryLogcatEntry(prio = 9, tag = "AlsoUnknown") +
                binaryLogcatEntry(prio = 4, tag = "Valid"),
        )

        assertEquals(1, records.size)
        assertEquals("Valid", records[0].raw.substring(records[0].tag))
    }

    @Test
    fun `empty tag replaced with placeholder`() {
        val records = parse(binaryLogcatEntry(tag = "", message = "msg"))

        assertEquals(1, records.size)
        assertEquals("?", records[0].raw.substring(records[0].tag))
        assertEquals("msg", records[0].raw.substring(records[0].message))
    }

    @Test
    fun `empty message replaced with space`() {
        val records = parse(binaryLogcatEntry(tag = "Tag", message = ""))

        assertEquals(1, records.size)
        assertEquals(" ", records[0].raw.substring(records[0].message))
    }

    @Test
    fun `resync after garbage bytes`() {
        val records = parse(ByteArray(4) + binaryLogcatEntry(tag = "AfterGarbage"))

        assertEquals(1, records.size)
        assertEquals("AfterGarbage", records[0].raw.substring(records[0].tag))
    }

    @Test
    fun `empty input produces no records`() {
        val records = parse(ByteArray(0))
        assertEquals(0, records.size)
    }

    private fun parse(vararg chunks: ByteArray): List<RawLogRecord> {
        val session = LogcatBinaryLogParser.Session()
        return chunks.flatMap { session.onChunk(it) }
    }

    private companion object {
        val TIME_REGEX = Regex("""\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}""")
    }
}
