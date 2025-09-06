package ru.vladislavsumin.qa.domain.logs

/**
 * Набор правил для фильтрации.
 *
 * Правила с одинаковым [Field] применяются через операцию ИЛИ.
 * Правила с разными [Field] применяются через операцию И.
 */
data class FilterRequest(
    val filters: Map<Field, List<Operation>>,
) {

    /**
     * Поле в логах к которому нужно применять операцию.
     */
    enum class Field {
        All,

        Level,
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
