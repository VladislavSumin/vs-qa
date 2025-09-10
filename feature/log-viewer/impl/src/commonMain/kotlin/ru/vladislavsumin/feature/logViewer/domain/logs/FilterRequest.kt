package ru.vladislavsumin.feature.logViewer.domain.logs

/**
 * Набор правил для фильтрации.
 *
 * Правила с одинаковым [Field] применяются через операцию ИЛИ.
 * Правила с разными [Field] применяются через операцию И.
 */
data class FilterRequest(
    val minLevel: LogLevel? = null,
    val filters: Map<Field, List<Operation>> = emptyMap(),

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
