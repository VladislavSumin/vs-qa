package ru.vladislavsumin.feature.logParser.domain.runId

/**
 * Информация о конкретном запуске. (В логах может быть несколько разных запусков приложения и полезно понимать к
 * какому запуску относятся текущие логи).
 *
 * @param startIndex индекс первой записи входящей в этот запуск.
 * @param meta любая мета информация о запуске.
 */
data class RawRunIdInfo(
    val startIndex: Int,
    val meta: Map<String, String>,
) {
    constructor(startIndex: Int, vararg meta: Pair<String, String>) : this(startIndex, meta.toMap())
}
