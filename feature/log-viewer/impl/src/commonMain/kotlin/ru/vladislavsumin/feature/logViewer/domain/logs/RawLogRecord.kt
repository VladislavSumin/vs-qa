package ru.vladislavsumin.feature.logViewer.domain.logs

import java.time.Instant

/**
 * Сырая log запись.
 *
 * @param line номер первой строки с которой начинается этот лог.
 * @param order номер лога (так как одна запись может занимать несколько строк, то этот параметер в общем случаее
 * не равен параметру [line].
 * @param raw сырая log запись не разбитая на подкомпоненты.
 *
 * Все прочие параметры являются слайсами [raw] строки. Так сделано для экономии памяти что бы не держать в памяти
 * несколько похожих компий одной строки.
 *
 * @param lines общее количество строк в записи.
 */
data class RawLogRecord(
    // val line: Int,
    val order: Int,
    val raw: String,
    val time: IntRange,
    val timeInstant: Instant,
    val level: IntRange,
    val thread: IntRange,
    val tag: IntRange,
    val message: IntRange,

    val logLevel: LogLevel,
    val lines: Int,
) {
    /**
     * Копирует модель с заменой поля [tag] в [raw] записи с корректным сохранением всех [IntRange]
     */
    fun copyTag(newTag: String): RawLogRecord {
        val newRaw = raw.replaceRange(tag, newTag)
        val newTagRange = IntRange(tag.first, tag.first + newTag.length - 1)
        val lenDelta = newTag.length - tag.count()

        return copy(
            raw = newRaw,
            tag = newTagRange,
            time = time.moveIfAfterPosition(newTagRange.first, lenDelta),
            level = level.moveIfAfterPosition(newTagRange.first, lenDelta),
            thread = thread.moveIfAfterPosition(newTagRange.first, lenDelta),
            message = message.moveIfAfterPosition(newTagRange.first, lenDelta),
        )
    }

    companion object {
        /**
         * Если [this] расположен после [position] то он сдвигается на [offset]. А если расположен до [position],
         * то возвращается оригинальный [this] без модификации.
         */
        private fun IntRange.moveIfAfterPosition(position: Int, offset: Int): IntRange {
            return if (first >= position) {
                IntRange(first + offset, last + offset)
            } else {
                this
            }
        }
    }
}
