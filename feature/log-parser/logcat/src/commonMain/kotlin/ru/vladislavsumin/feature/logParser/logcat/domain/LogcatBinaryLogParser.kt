package ru.vladislavsumin.feature.logParser.logcat.domain

import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Парсер бинарного logcat формата (`logcat --binary`).
 *
 * Поток состоит из последовательности `struct logger_entry` (little-endian):
 * ```
 * u16 len       — длина payload
 * u16 hdr_size  — размер заголовка (20 v1, 24 v2/v3, 28 v4)
 * i32 pid
 * u32 tid
 * u32 sec       — секунды epoch
 * u32 nsec
 * u32 lid       — id лог буфера (v3+)
 * u32 uid       — v4
 * ```
 * Payload текстовых буферов: `prio(1 байт) + tag\0 + message\0`.
 */
@Suppress("MagicNumber")
object LogcatBinaryLogParser {
    /**
     * Stateful обработчик потока бинарных данных, корректно обрабатывает записи,
     * разрезанные по границам чанков.
     */
    class Session {
        private var buffer: ByteArray = EMPTY_BUFFER

        /**
         * Обрабатывает очередной чанк данных.
         *
         * @return полностью полученные на данный момент записи.
         */
        fun onChunk(chunk: ByteArray): List<RawLogRecord> {
            buffer = if (buffer.isEmpty()) chunk else buffer + chunk

            val result = mutableListOf<RawLogRecord>()
            var offset = 0
            while (true) {
                val consumed = tryParseNext(buffer, offset, result)
                if (consumed == 0) break
                offset += consumed
            }
            buffer = if (offset == 0) buffer else buffer.copyOfRange(offset, buffer.size)
            return result
        }

        /**
         * @return количество потребленных байт, 0 если данных пока недостаточно.
         */
        @Suppress("ReturnCount")
        private fun tryParseNext(buf: ByteArray, offset: Int, result: MutableList<RawLogRecord>): Int {
            if (buf.size - offset < ENTRY_PREFIX_SIZE) return 0
            val payloadLen = readU16(buf, offset)
            val hdrSize = readU16(buf, offset + 2)

            if (hdrSize != HEADER_SIZE_V1 && hdrSize != HEADER_SIZE_V3 && hdrSize != HEADER_SIZE_V4) {
                // Рассинхронизация формата, пробуем ресинхронизироваться со следующего байта.
                LogcatLogger.e { "Unexpected logcat binary header size $hdrSize, skip one byte" }
                return 1
            }

            val totalSize = hdrSize + payloadLen
            if (buf.size - offset < totalSize) return 0

            parseEntry(buf, offset, hdrSize, payloadLen)?.let(result::add)
            return totalSize
        }
    }

    @Suppress("ReturnCount")
    private fun parseEntry(buf: ByteArray, offset: Int, hdrSize: Int, payloadLen: Int): RawLogRecord? {
        val pid = readS32(buf, offset + 4)
        val tid = readU32(buf, offset + 8)
        val sec = readU32(buf, offset + 12)
        val nsec = readU32(buf, offset + 16)
        val lid = if (hdrSize >= HEADER_SIZE_V3) readU32(buf, offset + 20) else LID_MAIN

        // Буферы с бинарным payload (events/stats/security) не поддерживаются.
        if (lid == LID_EVENTS || lid == LID_STATS || lid == LID_SECURITY) return null
        if (payloadLen < MIN_PAYLOAD_SIZE) return null

        val payloadStart = offset + hdrSize
        val payloadEnd = payloadStart + payloadLen

        val logLevel = prioToLevel(buf[payloadStart].toInt()) ?: return null

        var tagEnd = payloadStart + 1
        while (tagEnd < payloadEnd && buf[tagEnd] != 0.toByte()) tagEnd++
        if (tagEnd >= payloadEnd) return null
        val tag = String(buf, payloadStart + 1, tagEnd - payloadStart - 1, Charsets.UTF_8)
            .ifEmpty { "?" }

        var messageEnd = payloadEnd
        while (messageEnd > tagEnd + 1 &&
            (buf[messageEnd - 1] == 0.toByte() || buf[messageEnd - 1] == '\n'.code.toByte())
        ) {
            messageEnd--
        }
        val message = String(buf, tagEnd + 1, messageEnd - tagEnd - 1, Charsets.UTF_8)
            .ifEmpty { " " }

        return createRecord(
            date = TIME_FORMATTER.format(Instant.ofEpochSecond(sec, nsec)),
            timeInstant = Instant.ofEpochSecond(sec, nsec),
            pid = pid.toString(),
            tid = tid.toString(),
            logLevel = logLevel,
            tag = tag,
            message = message,
        )
    }

    @Suppress("LongParameterList")
    private fun createRecord(
        date: String,
        timeInstant: Instant,
        pid: String,
        tid: String,
        logLevel: LogLevel,
        tag: String,
        message: String,
    ): RawLogRecord {
        val raw = "$date $pid:$tid ${LEVEL_ALIASES.getValue(logLevel)} $tag $message"

        val dateRange = LogRange(0, date.length - 1)
        val pidRange = LogRange(dateRange.last + 2, dateRange.last + 1 + pid.length)
        val tidRange = LogRange(pidRange.last + 2, pidRange.last + 1 + tid.length)
        val levelRange = LogRange(tidRange.last + 2, tidRange.last + 2)
        val tagRange = LogRange(levelRange.last + 2, levelRange.last + 1 + tag.length)

        return RawLogRecord(
            raw = raw,
            time = dateRange,
            timeDate = LogRange(0, DATE_PART_LAST_INDEX),
            timeInstant = timeInstant,
            processId = pidRange,
            thread = tidRange,
            level = levelRange,
            tag = tagRange,
            message = LogRange(tagRange.last + 2, raw.length - 1),
            logLevel = logLevel,
            lines = raw.lines().size,
        )
    }

    private fun prioToLevel(prio: Int): LogLevel? = when (prio) {
        2 -> LogLevel.VERBOSE
        3 -> LogLevel.DEBUG
        4 -> LogLevel.INFO
        5 -> LogLevel.WARN
        6 -> LogLevel.ERROR
        7 -> LogLevel.FATAL
        else -> null
    }

    private fun readU16(buf: ByteArray, offset: Int): Int =
        (buf[offset].toInt() and 0xFF) or ((buf[offset + 1].toInt() and 0xFF) shl 8)

    private fun readS32(buf: ByteArray, offset: Int): Int = (buf[offset].toInt() and 0xFF) or
        ((buf[offset + 1].toInt() and 0xFF) shl 8) or
        ((buf[offset + 2].toInt() and 0xFF) shl 16) or
        ((buf[offset + 3].toInt() and 0xFF) shl 24)

    private fun readU32(buf: ByteArray, offset: Int): Long = readS32(buf, offset).toLong() and 0xFFFF_FFFF

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault())

    private val LEVEL_ALIASES = mapOf(
        LogLevel.VERBOSE to "V",
        LogLevel.DEBUG to "D",
        LogLevel.INFO to "I",
        LogLevel.WARN to "W",
        LogLevel.ERROR to "E",
        LogLevel.FATAL to "F",
    )

    private val EMPTY_BUFFER = ByteArray(0)

    private const val ENTRY_PREFIX_SIZE = 4
    private const val HEADER_SIZE_V1 = 20
    private const val HEADER_SIZE_V3 = 24
    private const val HEADER_SIZE_V4 = 28

    // prio + пустой tag с \0 + минимум 1 байт сообщения
    private const val MIN_PAYLOAD_SIZE = 3

    // Индекс последнего символа датной части "MM-dd" в строке времени "MM-dd HH:mm:ss.SSS"
    private const val DATE_PART_LAST_INDEX = 4

    private const val LID_MAIN = 0L
    private const val LID_EVENTS = 2L
    private const val LID_STATS = 5L
    private const val LID_SECURITY = 6L
}
