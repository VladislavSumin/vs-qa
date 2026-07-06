package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("MaximumLineLength", "MaxLineLength")
class AnimeEmbeddedLogParserTest {

    @Test
    fun `single line entry`() {
        val input = sequenceOf(
            "2024-06-15T+03:00 12:30:45.123 main D MyTag Hello world",
        )
        val result = parse(input)

        assertEquals(1, result.size)
        val record = result[0]
        assertEquals(1, record.lines)
        assertEquals("2024-06-15T+03:00 12:30:45.123 main D MyTag Hello world", record.raw)
        assertEquals("2024-06-15T+03:00 12:30:45.123", record.raw.substring(record.time))
        assertEquals("2024-06-15T+03:00", record.raw.substring(record.timeDate))
        assertEquals("main", record.raw.substring(record.thread))
        assertEquals("D", record.raw.substring(record.level))
        assertEquals(LogLevel.DEBUG, record.logLevel)
        assertEquals("MyTag", record.raw.substring(record.tag))
        assertEquals("Hello world", record.raw.substring(record.message))
        assertTrue(record.timeInstant > Instant.EPOCH)
    }

    @Test
    fun `two single line entries`() {
        val input = sequenceOf(
            "2024-06-15T+03:00 12:30:45.123 main D MyTag First message",
            "2024-06-15T+03:00 12:30:46.001 worker I App Init completed",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals("First message", result[0].raw.substring(result[0].message))
        assertEquals(1, result[0].lines)
        assertEquals("Init completed", result[1].raw.substring(result[1].message))
        assertEquals(1, result[1].lines)
        assertEquals("worker", result[1].raw.substring(result[1].thread))
        assertEquals(LogLevel.INFO, result[1].logLevel)
        assertEquals("App", result[1].raw.substring(result[1].tag))
    }

    @Test
    fun `multi-line entry with stacktrace`() {
        val input = sequenceOf(
            "2024-06-15T+03:00 14:25:22.500 main E CrashHandler FATAL EXCEPTION",
            "java.lang.RuntimeException: Test error",
            "\tat com.example.MyClass.doWork(MyClass.kt:42)",
            "\tat com.example.App.run(App.kt:15)",
            "2024-06-15T+03:00 14:25:23.000 other D Tag Next entry",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val crash = result[0]
        assertEquals(4, crash.lines)
        assertEquals(LogLevel.ERROR, crash.logLevel)
        assertEquals("CrashHandler", crash.raw.substring(crash.tag))
        val message = crash.raw.substring(crash.message)
        assertTrue(message.startsWith("FATAL EXCEPTION"))
        assertTrue(message.contains("java.lang.RuntimeException"))
        assertTrue(message.contains("at com.example.MyClass.doWork"))

        val next = result[1]
        assertEquals(1, next.lines)
        assertEquals("Next entry", next.raw.substring(next.message))
    }

    @Test
    fun `utc timezone Z format`() {
        val input = sequenceOf(
            "2024-01-01TZ 00:00:00.000 main I UTC midnight event",
        )
        val result = parse(input)

        assertEquals(1, result.size)
        assertEquals("2024-01-01TZ 00:00:00.000", result[0].raw.substring(result[0].time))
        assertEquals("2024-01-01TZ", result[0].raw.substring(result[0].timeDate))
        assertTrue(result[0].timeInstant > Instant.EPOCH)
    }

    @Test
    fun `timezone positive offset`() {
        val input = sequenceOf(
            "2024-06-15T+05:30 10:00:00.000 main I MyTag event in India",
        )
        val result = parse(input)

        assertEquals(1, result.size)
        assertEquals("2024-06-15T+05:30 10:00:00.000", result[0].raw.substring(result[0].time))
        assertEquals("MyTag", result[0].raw.substring(result[0].tag))
        assertEquals("event in India", result[0].raw.substring(result[0].message))
    }

    @Test
    fun `all log levels`() {
        val input = sequenceOf(
            "2024-01-01T+00:00 00:00:00.000 t1 V TraceTag trace",
            "2024-01-01T+00:00 00:00:01.000 t2 D DebugTag debug",
            "2024-01-01T+00:00 00:00:02.000 t3 I InfoTag info",
            "2024-01-01T+00:00 00:00:03.000 t4 W WarnTag warn",
            "2024-01-01T+00:00 00:00:04.000 t5 E ErrorTag error",
            "2024-01-01T+00:00 00:00:05.000 t6 F FatalTag fatal",
        )
        val result = parse(input)

        assertEquals(6, result.size)
        assertEquals(LogLevel.VERBOSE, result[0].logLevel)
        assertEquals(LogLevel.DEBUG, result[1].logLevel)
        assertEquals(LogLevel.INFO, result[2].logLevel)
        assertEquals(LogLevel.WARN, result[3].logLevel)
        assertEquals(LogLevel.ERROR, result[4].logLevel)
        assertEquals(LogLevel.FATAL, result[5].logLevel)
    }

    @Test
    fun `empty message`() {
        val input = sequenceOf(
            "2024-01-01T+00:00 00:00:00.000 t1 D MyTag ",
            "2024-01-01T+00:00 00:00:01.000 t2 I App next",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val emptyMsg = result[0]
        assertEquals("", emptyMsg.raw.substring(emptyMsg.message))
        assertEquals(1, emptyMsg.lines)

        assertEquals("next", result[1].raw.substring(result[1].message))
    }

    @Test
    fun `tag with dots and special chars`() {
        val input = sequenceOf(
            "2024-01-01T+00:00 00:00:00.000 pool-1-thread-5 D one.me.sdk.core.Initializer init start",
            "2024-01-01T+00:00 00:00:01.000 main I [Tracker] event sent",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals("one.me.sdk.core.Initializer", result[0].raw.substring(result[0].tag))
        assertEquals("[Tracker]", result[1].raw.substring(result[1].tag))
    }

    @Test
    fun `multiple multi-line entries in sequence`() {
        val input = sequenceOf(
            "2024-01-01T+00:00 00:00:00.000 t1 E TagA error one",
            "  detail line 1",
            "  detail line 2",
            "2024-01-01T+00:00 00:00:01.000 t2 E TagB error two",
            "  detail line 3",
            "2024-01-01T+00:00 00:00:02.000 t3 I TagC ok",
        )
        val result = parse(input)

        assertEquals(3, result.size)
        assertEquals(3, result[0].lines)
        assertEquals(2, result[1].lines)
        assertEquals(1, result[2].lines)
    }

    @Test
    fun `json in message`() {
        val input = sequenceOf(
            "2024-01-01T+00:00 00:00:00.000 t1 D Net response body: {\"status\":\"ok\",\"count\":3}",
        )
        val result = parse(input)

        assertEquals(1, result.size)
        val msg = result[0].raw.substring(result[0].message)
        assertTrue(msg.contains("\"status\""))
        assertTrue(msg.contains("\"count\""))
    }

    @Test
    fun `orphan lines before first header included in first entry`() {
        val input = sequenceOf(
            "orphan line without header",
            "2024-01-01T+00:00 00:00:00.000 t1 I Tag valid entry",
        )
        val result = parse(input)

        assertEquals(1, result.size)
        val raw = result[0].raw
        assertTrue(raw.startsWith("orphan line"), "orphan line included at start")
        assertTrue(raw.contains("valid entry"), "valid entry is in the raw text")
        assertEquals(2, result[0].lines)
    }

    @Test
    fun `very long multi-line entry`() {
        val lines = mutableListOf("2024-06-15T+03:00 12:00:00.000 main E BigTag start of long trace")
        repeat(20) { i ->
            lines.add("\tat com.example.Layer$i.method(Layer$i.kt:$i)")
        }
        val result = parse(lines.asSequence())

        assertEquals(1, result.size)
        assertEquals(21, result[0].lines)
        assertEquals(LogLevel.ERROR, result[0].logLevel)
        assertTrue(result[0].raw.lines().size == 21)
    }

    @Test
    fun `empty input`() {
        val result = parse(emptySequence())
        assertEquals(0, result.size)
    }

    private fun parse(lines: Sequence<String>): List<RawLogRecord> {
        val result = mutableListOf<RawLogRecord>()
        AnimeEmbeddedLogParser.parseLines(lines, result)
        return result
    }
}
