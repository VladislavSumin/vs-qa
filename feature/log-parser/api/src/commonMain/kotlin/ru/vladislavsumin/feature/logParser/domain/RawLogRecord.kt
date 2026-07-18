package ru.vladislavsumin.feature.logParser.domain

import java.time.Instant

/**
 * Сырая log запись.
 *
 * @param raw сырая log запись не разбитая на подкомпоненты.
 *
 * Все прочие параметры являются слайсами [raw] строки. Так сделано для экономии памяти что бы не держать в памяти
 * несколько похожих компий одной строки.
 *
 * @param lines общее количество строк в записи.
 */
data class RawLogRecord(
    val raw: String,

    val time: LogRange,
    val timeDate: LogRange,
    val timeInstant: Instant,

    val level: LogRange,
    val logLevel: LogLevel,

    val processId: LogRange?, // pid
    val thread: LogRange, // tid
    val tag: LogRange,
    val message: LogRange,

    val lines: Int,

    /**
     * Имя процесса, опционально добавляется обогащением через [withProcessName].
     */
    val processName: LogRange? = null,
) {
    /**
     * Копирует модель с заменой поля [tag] в [raw] записи с корректным сохранением всех [LogRange]
     */
    fun copyTag(newTag: String): RawLogRecord {
        val newRaw = raw.replaceRange(tag, newTag)
        val newTagRange = LogRange(tag.first, tag.first + newTag.length - 1)
        val lenDelta = newTag.length - (tag.last - tag.first + 1)

        return copy(
            raw = newRaw,
            tag = newTagRange,
            time = time.moveIfAfterPosition(newTagRange.first, lenDelta),
            level = level.moveIfAfterPosition(newTagRange.first, lenDelta),
            thread = thread.moveIfAfterPosition(newTagRange.first, lenDelta),
            message = message.moveIfAfterPosition(newTagRange.first, lenDelta),
            processName = processName?.moveIfAfterPosition(newTagRange.first, lenDelta),
        )
    }

    /**
     * Копирует модель, вставляя имя процесса в [raw] сразу после блока `PID:TID`
     * с корректным сдвигом всех последующих [LogRange].
     */
    fun withProcessName(name: String): RawLogRecord {
        if (name.isEmpty()) return this

        val insertPosition = thread.last + 2
        val newRaw = buildString {
            append(raw, 0, insertPosition)
            append(name)
            append(' ')
            append(raw, insertPosition, raw.length)
        }
        val delta = name.length + 1

        return copy(
            raw = newRaw,
            processName = LogRange(insertPosition, insertPosition + name.length - 1),
            time = time.moveIfAfterPosition(insertPosition, delta),
            level = level.moveIfAfterPosition(insertPosition, delta),
            tag = tag.moveIfAfterPosition(insertPosition, delta),
            message = message.moveIfAfterPosition(insertPosition, delta),
        )
    }
}
