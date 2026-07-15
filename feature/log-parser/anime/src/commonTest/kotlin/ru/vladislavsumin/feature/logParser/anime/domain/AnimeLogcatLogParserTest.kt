package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.substring
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("MaximumLineLength", "MaxLineLength")
class AnimeLogcatLogParserTest {

    init {
        LoggerManager.initTest()
    }

    @Test
    fun `entries with meta headers`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 D/MyTag ]",
            "process started",
            "init complete",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000 12345:67890 I/NextOne ]",
            "service started",
            "ready",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val first = result[0]
        assertEquals(2, first.lines)
        assertEquals("01-02 10:20:30.123", first.raw.substring(first.time))
        assertEquals("12345", first.raw.substring(first.processId!!))
        assertEquals("67890", first.raw.substring(first.thread))
        assertEquals("D", first.raw.substring(first.level))
        assertEquals(LogLevel.DEBUG, first.logLevel)
        assertEquals("MyTag", first.raw.substring(first.tag))
        assertTrue(first.raw.substring(first.message).contains("process started"))
        assertTrue(first.raw.substring(first.message).contains("init complete"))
        assertEquals("NextOne", result[1].raw.substring(result[1].tag))
        assertEquals(LogLevel.INFO, result[1].logLevel)
    }

    @Test
    fun `entries with varying pid spacing`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123   123:456 D/SomeTag ]",
            "message one",
            "detail one",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000  7890:12 I/Another ]",
            "message two",
            "detail two",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals("123", result[0].raw.substring(result[0].processId!!))
        assertEquals("456", result[0].raw.substring(result[0].thread))
        assertEquals(2, result[0].lines)
        assertEquals(LogLevel.INFO, result[1].logLevel)
    }

    @Test
    fun `multi-line entry with stacktrace`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 E/AndroidRuntime ]",
            "FATAL EXCEPTION: main",
            "java.lang.RuntimeException: Crash",
            "\tat com.example.MyClass.boom(MyClass.kt:99)",
            "\tat com.example.App.main(App.kt:10)",
            "--------- beginning of crash",
            "[ 01-02 10:20:31.000 12345:67890 D/NextTag ]",
            "next msg one",
            "next msg two",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val crash = result[0]
        assertEquals(4, crash.lines)
        assertEquals(LogLevel.ERROR, crash.logLevel)
        assertEquals("AndroidRuntime", crash.raw.substring(crash.tag))
        assertTrue(crash.raw.substring(crash.message).contains("FATAL EXCEPTION"))
        assertTrue(crash.raw.substring(crash.message).contains("RuntimeException: Crash"))
        assertTrue(crash.raw.substring(crash.message).contains("at com.example.MyClass.boom"))
        assertEquals("NextTag", result[1].raw.substring(result[1].tag))
        assertEquals(2, result[1].lines)
    }

    @Test
    fun `meta header stripped from entry body`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 E/CrashHandler ]",
            "Process: com.example.app",
            "version 1.0",
            "--------- beginning of crash",
            "[ 01-02 10:20:31.000 12345:67890 D/NextTag ]",
            "Normal message",
            "detail",
            "--------- beginning of system",
            "[ 01-02 10:20:32.000 12345:67890 I/Last ]",
            "final msg one",
            "final msg two",
        )
        val result = parse(input)

        assertEquals(3, result.size)
        assertEquals(2, result[0].lines)
        assertTrue(result[0].raw.substring(result[0].message).contains("Process: com.example.app"))
        assertEquals(2, result[1].lines)
        assertEquals(LogLevel.DEBUG, result[1].logLevel)
    }

    @Test
    fun `consecutive meta headers at end of entry`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 E/Tag ]",
            "content line one",
            "content line two",
            "--------- beginning of main",
            "--------- beginning of system",
            "--------- beginning of crash",
            "[ 01-02 10:20:31.000 12345:67890 D/Next ]",
            "done one",
            "done two",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val entry = result[0]
        assertEquals(2, entry.lines)
        assertTrue(entry.raw.substring(entry.message).contains("content line one"))
        assertTrue(entry.raw.substring(entry.message).contains("content line two"))
    }

    @Test
    fun `truncated entry preserves partial lines`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 E/CrashHandler ]",
            "FATAL EXCEPTION",
            "at com.exam",
            "[ 01-02 10:20:31.000 12345:67890 D/NextTag ]",
            "next msg one",
            "next msg two",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val crash = result[0]
        assertTrue(crash.raw.substring(crash.message).contains("at com.exam"))
        assertEquals(2, crash.lines)
    }

    @Test
    fun `all log levels`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.000 1:2 V/VerboseTag ]", "trace msg", "trace detail",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000 1:2 D/DebugTag ]", "debug msg", "debug detail",
            "--------- beginning of events",
            "[ 01-02 10:20:32.000 1:2 I/InfoTag ]", "info msg", "info detail",
            "--------- beginning of kernel",
            "[ 01-02 10:20:33.000 1:2 W/WarnTag ]", "warn msg", "warn detail",
            "--------- beginning of crash",
            "[ 01-02 10:20:34.000 1:2 E/ErrorTag ]", "error msg", "error detail",
            "--------- beginning of main",
            "[ 01-02 10:20:35.000 1:2 F/FatalTag ]", "fatal msg", "fatal detail",
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
    fun `lines before first meta header are ignored`() {
        val input = sequenceOf(
            "random output before logcat starts",
            "another garbage line",
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 D/Tag ]",
            "valid content",
            "more content",
            "--------- beginning of crash",
            "[ 01-02 10:20:31.000 12345:67890 I/Next ]",
            "next content",
            "even more",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        val first = result[0]
        assertEquals(2, first.lines)
        assertTrue(!first.raw.contains("random output"))
        assertEquals("Tag", first.raw.substring(first.tag))
    }

    @Test
    fun `header with trailing spaces`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 D/Tag   ]",
            "content one",
            "content two",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000 12345:67890 D/Next ]",
            "next one",
            "next two",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals("Tag", result[0].raw.substring(result[0].tag))
    }

    @Test
    fun `single digit pid and tid`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 1:2 D/Tag ]",
            "msg1",
            "msg2",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000 999:8 I/Next ]",
            "msg3",
            "msg4",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals("1", result[0].raw.substring(result[0].processId!!))
        assertEquals("2", result[0].raw.substring(result[0].thread))
    }

    @Test
    fun `very long tid`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:999999 D/Tag ]",
            "msg1",
            "msg2",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000 1:1 I/Next ]",
            "msg3",
            "msg4",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals("12345", result[0].raw.substring(result[0].processId!!))
        assertEquals("999999", result[0].raw.substring(result[0].thread))
    }

    @Test
    fun `empty input`() {
        val result = parse(emptySequence())
        assertEquals(0, result.size)
    }

    @Test
    fun `only non-header lines`() {
        val input = sequenceOf(
            "just some text",
            "no logcat headers here",
        )
        val result = parse(input)
        assertEquals(0, result.size)
    }

    @Test
    fun `entry without meta header before next entry`() {
        val input = sequenceOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 E/AndroidRuntime ]",
            "FATAL EXCEPTION in thread main",
            "\tat a.b.c(Unknown Source)",
            "[ 01-02 10:20:31.000 12345:67890 D/NormalTag ]",
            "normal msg one",
            "normal msg two",
        )
        val result = parse(input)

        assertEquals(2, result.size)
        assertEquals(2, result[0].lines)
        assertTrue(result[0].raw.substring(result[0].message).contains("FATAL EXCEPTION"))
        assertTrue(result[0].raw.substring(result[0].message).contains("at a.b.c"))
    }

    private fun parse(lines: Sequence<String>): List<RawLogRecord> {
        val result = mutableListOf<RawLogRecord>()
        AnimeLogcatLogParser.parseLines(lines, result)
        return result
    }
}
