package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Immutable
import ru.vladislavsumin.qa.domain.logs.LogRecord

/**
 * @param filter строка для фильтрации логов
 * @param isFilterUseRegex использует ли фильтр регулярные выражения для поиска.
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
data class LogViewerViewState(
    val filter: String,
    val search: String,
    val isFilterUseRegex: Boolean,
    val isSearchUseRegex: Boolean,
    val searchResults: Int,
    val selectedSearchIndex: Int,
    val searchIndex: List<Pair<Int, LogRecord>>,
    val logs: List<LogRecord>,
    val maxLogNumberDigits: Int,
)
