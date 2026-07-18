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

class LogcatBinaryFlowLogParserTest {

    init {
        LoggerManager.initTest()
    }

    private val parser = LogcatBinaryFlowLogParser()

    @Test
    fun `parses binary flow split by chunks`() = runTest {
        val first = binaryLogcatEntry(prio = 3, tag = "First", message = "first message")
        val second = binaryLogcatEntry(prio = 4, tag = "Second", message = "second message")
        val data = first + second

        val result = parser.parseLog(
            flowOf(
                data.copyOfRange(0, 10),
                data.copyOfRange(10, data.size),
            ),
        ).toList()

        assertEquals(2, result.size)
        assertEquals("First", result[0].raw.substring(result[0].tag))
        assertEquals(LogLevel.DEBUG, result[0].logLevel)
        assertEquals("Second", result[1].raw.substring(result[1].tag))
        assertEquals(LogLevel.INFO, result[1].logLevel)
    }

    @Test
    fun `emits record without waiting for flow completion`() = runTest {
        val data = flow {
            emit(binaryLogcatEntry(tag = "First"))
            emit(binaryLogcatEntry(tag = "Second"))
            awaitCancellation()
        }

        val record = parser.parseLog(data).first()

        assertEquals("First", record.raw.substring(record.tag))
    }

    @Test
    fun `empty flow produces no records`() = runTest {
        val result = parser.parseLog(flowOf<ByteArray>()).toList()
        assertEquals(0, result.size)
    }
}
