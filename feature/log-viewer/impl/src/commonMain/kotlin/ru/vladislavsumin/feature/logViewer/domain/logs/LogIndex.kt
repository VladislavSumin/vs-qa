package ru.vladislavsumin.feature.logViewer.domain.logs

/**
 * @param totalLogRecords общее количество записей в оригинальном списке (до фильтра).
 */
data class LogIndex(
    val logs: List<LogRecord>,
    val runIdOrders: List<RunIdInfo>?,
    val searchIndex: SearchIndex,
    val totalLogRecords: Int,
) {
    sealed interface SearchIndex {
        /**
         * Индекс поиска. Тут ключ это номер поискового результата,
         * а значение номер соответствующей записи в [LogIndex.logs].
         */
        val index: List<Int> get() = emptyList()

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
        data class Search(
            override val index: List<Int>,
        ) : SearchIndex

        /**
         * Ошибка поиска, некорректный Regex.
         */
        data object BadRegex : SearchIndex
    }
}
