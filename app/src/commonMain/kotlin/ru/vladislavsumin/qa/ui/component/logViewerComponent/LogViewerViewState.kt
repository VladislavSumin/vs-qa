package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Immutable
import ru.vladislavsumin.qa.domain.logs.RawLogRecord

/**
 * @param filter строка для фильтрации логов
 * @param isFilterUseRegex использует ли фильтр регулярные выражения для поиска.
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
data class LogViewerViewState(
    val filter: String,
    val isFilterUseRegex: Boolean,
    val logs: List<RawLogRecord>,
    val maxLogNumberDigits: Int,
)