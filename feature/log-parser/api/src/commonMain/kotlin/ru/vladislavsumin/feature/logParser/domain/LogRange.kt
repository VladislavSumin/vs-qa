package ru.vladislavsumin.feature.logParser.domain

@JvmInline
value class LogRange(val packed: Long) {
    @Suppress("MagicNumber")
    constructor(start: Int, endInclusive: Int) : this(
        (start.toLong() shl 32) or (endInclusive.toLong() and 0xFFFF_FFFF),
    )
    constructor(range: IntRange) : this(range.first, range.last)

    val start: Int get() = (packed shr 32).toInt()
    val endInclusive: Int get() = packed.toInt()
    val first: Int get() = start
    val last: Int get() = endInclusive

    companion object {
        val EMPTY: LogRange = LogRange(1, 0)
    }

    fun moveIfAfterPosition(position: Int, offset: Int): LogRange = if (first >= position) {
        LogRange(first + offset, last + offset)
    } else {
        this
    }
}

fun String.substring(range: LogRange): String = substring(range.start, range.endInclusive + 1)

fun String.replaceRange(range: LogRange, replacement: String): String =
    replaceRange(range.start, range.endInclusive + 1, replacement)
