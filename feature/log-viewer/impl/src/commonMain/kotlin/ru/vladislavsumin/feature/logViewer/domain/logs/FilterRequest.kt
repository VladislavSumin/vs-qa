package ru.vladislavsumin.feature.logViewer.domain.logs

import ru.vladislavsumin.feature.logParser.domain.LogLevel

/**
 * Набор правил для фильтрации.
 *
 * Правила с одинаковым [Field] применяются через операцию ИЛИ.
 * Правила с разными [Field] применяются через операцию И.
 */
data class FilterRequest(
    val minLevel: LogLevel? = null,
    val filters: Map<Field, List<Operation>> = emptyMap(),
    val runOrders: List<Int> = emptyList(),

    // TODO это конечно прям совсем в тупую да
    val timeAfter: String? = null,
    val timeBefore: String? = null,
) {

    /**
     * Поле в логах к которому нужно применять операцию.
     */
    enum class Field {
        All,

        Tag,
        ProcessId,
        Thread,
        Message,
    }

    /**
     * Операция поиска.
     */
    sealed interface Operation {
        data class Exactly(val data: String) : Operation
        data class Contains(val data: String) : Operation
    }
}
