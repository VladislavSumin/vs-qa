package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.filterBar.FilterRequestParser
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.searchBar.LogSearchBarViewState

/**
 * @param filter строка для фильтрации логов
 * @param isFilterUseRegex использует ли фильтр регулярные выражения для поиска.
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
internal data class LogViewerViewState(
    val filterField: TextFieldValue,
    val filter: FilterRequestParser.RequestHighlight,
    val isFilterValid: Boolean,
    val searchIndex: List<Int>,
    val logs: List<LogRecord>,
    val maxLogNumberDigits: Int,
    val searchState: LogSearchBarViewState,
)
