package ru.vladislavsumin.feature.logViewer.domain.logs

/**
 * Отображает процесс фильтрации и поиска.
 *
 * @param isFilteringNow идет ли в данный момент прогресс фильтрации.
 * @param isSearchingNow идет ли в данный момент прогресс поиска.
 * @param lastSuccessIndex индекс логов для последнего завершенного поиска, если все параметры поиска в false то поиск
 * актуальный, если хотя бы один в true, то это результат последнего завершенного до конца поиска.
 */
data class LogIndexProgress(
    val isFilteringNow: Boolean,
    val isSearchingNow: Boolean,
    val lastSuccessIndex: LogIndex,
)
