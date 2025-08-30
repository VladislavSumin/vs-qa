package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Immutable
import ru.vladislavsumin.qa.domain.logs.RawLogRecord

/**
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
data class LogViewerViewState(
    val filter: String,
    val logs: List<RawLogRecord>,
    val maxLogNumberDigits: Int,
)