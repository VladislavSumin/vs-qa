package ru.vladislavsumin.qa.domain.logs

data class LogIndex(
    val logs: List<LogRecord>,
    // val searchIndex: SearchIndex,
) {
    sealed interface SearchIndex {
        /**
         * Пустой поисковый запрос.
         */
        data object NoSearch : SearchIndex

        /**
         * Поиск с нулевым количеством совпадений.
         */
        data object EmptySearch : SearchIndex

        /**
         * Поиск с как минимум одним результатом.
         */
        data object Search : SearchIndex
    }
}
