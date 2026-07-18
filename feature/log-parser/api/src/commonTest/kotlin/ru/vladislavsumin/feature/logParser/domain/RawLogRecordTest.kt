package ru.vladislavsumin.feature.logParser.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class RawLogRecordTest {

    @Test
    fun `withProcessName inserts name after pid tid block`() {
        val record = record()

        val enriched = record.withProcessName("com.app:push")

        assertEquals("01-02 10:20:30.123 123:456 com.app:push I MyTag some message", enriched.raw)
        assertEquals("com.app:push", enriched.raw.substring(enriched.processName!!))
    }

    @Test
    fun `withProcessName keeps all ranges valid`() {
        val record = record()

        val enriched = record.withProcessName("com.app:push")

        assertEquals("01-02 10:20:30.123", enriched.raw.substring(enriched.time))
        assertEquals("123", enriched.raw.substring(enriched.processId!!))
        assertEquals("456", enriched.raw.substring(enriched.thread))
        assertEquals("I", enriched.raw.substring(enriched.level))
        assertEquals("MyTag", enriched.raw.substring(enriched.tag))
        assertEquals("some message", enriched.raw.substring(enriched.message))
        assertEquals(record.timeInstant, enriched.timeInstant)
        assertEquals(record.logLevel, enriched.logLevel)
        assertEquals(record.lines, enriched.lines)
    }

    @Test
    fun `withProcessName with empty name returns same record`() {
        val record = record()

        val enriched = record.withProcessName("")

        assertSame(record, enriched)
        assertNull(enriched.processName)
    }

    @Test
    fun `copyTag preserves process name range`() {
        val enriched = record().withProcessName("com.app")

        val retagged = enriched.copyTag("VeryLongReplacementTag")

        assertEquals("com.app", retagged.raw.substring(retagged.processName!!))
        assertEquals("VeryLongReplacementTag", retagged.raw.substring(retagged.tag))
        assertEquals("some message", retagged.raw.substring(retagged.message))
    }

    private fun record(): RawLogRecord {
        val date = "01-02 10:20:30.123"
        val pid = "123"
        val tid = "456"
        val tag = "MyTag"
        val message = "some message"
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
}
