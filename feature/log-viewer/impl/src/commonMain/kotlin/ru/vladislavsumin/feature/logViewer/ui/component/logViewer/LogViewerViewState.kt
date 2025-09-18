package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Immutable
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.searchBar.LogSearchBarViewState
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsViewState

/**
 * @param filter строка для фильтрации логов
 * @param isFilterUseRegex использует ли фильтр регулярные выражения для поиска.
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
internal data class LogViewerViewState(
    val searchIndex: List<Int>,
    val logsViewState: LogsViewState,
    val searchState: LogSearchBarViewState,
    val isMappingApplied: Boolean,
    val showSelectMappingDialog: Boolean,
    val showDragAndDropContainers: Boolean,
) {
    companion object {
        val STUB = LogViewerViewState(
            searchIndex = emptyList(),
            logsViewState = LogsViewState(
                logs = emptyList(),
                maxLogNumberDigits = 0,
            ),
            searchState = LogSearchBarViewState.STUB,
            isMappingApplied = false,
            showSelectMappingDialog = false,
            showDragAndDropContainers = false,
        )
    }
}
