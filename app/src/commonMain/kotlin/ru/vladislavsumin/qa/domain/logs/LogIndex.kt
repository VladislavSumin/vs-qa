package ru.vladislavsumin.qa.domain.logs

data class LogIndex(
    val logs: List<LogRecord>,
    val searchIndex: SearchIndex,
) {
    sealed interface SearchIndex {
        /**
         * Индекс поиска. Тут ключ это номер поискового результата,
         * а значение номер соответствующей записи в [LogIndex.logs].
         */
        val index: List<Int>

        /**
         * Пустой поисковый запрос.
         */
        data object NoSearch : SearchIndex {
            override val index: List<Int> = emptyList()
        }

        /**
         * Поиск с нулевым количеством совпадений.
         */
        data object EmptySearch : SearchIndex {
            override val index: List<Int> = emptyList()
        }

        /**
         * Поиск с как минимум одним результатом.
         */
        data class Search(
            override val index: List<Int>,
        ) : SearchIndex
    }
}
