package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Immutable
import ru.vladislavsumin.qa.domain.logs.LogRecord
import ru.vladislavsumin.qa.ui.component.logViewerComponent.searchBar.LogSearchBarViewState

/**
 * @param filter строка для фильтрации логов
 * @param isFilterUseRegex использует ли фильтр регулярные выражения для поиска.
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
internal data class LogViewerViewState(
    val filter: String,
    val isFilterUseRegex: Boolean,
    val searchIndex: List<Int>,
    val logs: List<LogRecord>,
    val maxLogNumberDigits: Int,
    val searchState: LogSearchBarViewState,
)
