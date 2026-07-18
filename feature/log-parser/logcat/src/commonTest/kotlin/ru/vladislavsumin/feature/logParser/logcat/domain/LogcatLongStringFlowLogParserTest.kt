package ru.vladislavsumin.feature.logParser.logcat.domain

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.substring
import kotlin.test.Test
import kotlin.test.assertEquals

class LogcatLongStringFlowLogParserTest {

    init {
        LoggerManager.initTest()
    }

    private val parser = LogcatLongStringFlowLogParser()

    @Test
    fun `parses string flow in logcat format`() = runTest {
        val lines = flowOf(
            "--------- beginning of main",
            "[ 01-02 10:20:30.123 12345:67890 D/MyTag ]",
            "process started",
            "init complete",
            "--------- beginning of system",
            "[ 01-02 10:20:31.000 12345:67890 I/NextOne ]",
            "service started",
            "ready",
        )
        val result = parser.parseLog(lines).toList()

        assertEquals(2, result.size)
        val first = result[0]
        assertEquals("01-02 10:20:30.123", first.raw.substring(first.time))
        assertEquals("12345", first.raw.substring(first.processId!!))
        assertEquals("67890", first.raw.substring(first.thread))
        assertEquals(LogLevel.DEBUG, first.logLevel)
        assertEquals("MyTag", first.raw.substring(first.tag))
        assertEquals(2, first.lines)
        val last = result[1]
        assertEquals("NextOne", last.raw.substring(last.tag))
        assertEquals(LogLevel.INFO, last.logLevel)
        assertEquals(2, last.lines)
    }

    @Test
    fun `emits record without waiting for flow completion`() = runTest {
        val lines = flow {
            emit("[ 01-02 10:20:30.123 12345:67890 D/MyTag ]")
            emit("first message")
            emit("second message")
            emit("[ 01-02 10:20:31.000 12345:67890 I/NextOne ]")
            awaitCancellation()
        }
        val record = parser.parseLog(lines).first()

        assertEquals("MyTag", record.raw.substring(record.tag))
        assertEquals(LogLevel.DEBUG, record.logLevel)
    }

    @Test
    fun `empty flow produces no records`() = runTest {
        val result = parser.parseLog(flowOf<String>()).toList()
        assertEquals(0, result.size)
    }
}
