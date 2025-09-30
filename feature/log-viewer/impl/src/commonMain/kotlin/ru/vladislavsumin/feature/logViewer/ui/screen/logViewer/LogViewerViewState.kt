package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.runtime.Immutable
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsViewState
import ru.vladislavsumin.feature.logViewer.ui.component.searchBar.SearchBarViewState

/**
 * @param filter строка для фильтрации логов
 * @param isFilterUseRegex использует ли фильтр регулярные выражения для поиска.
 * @param maxLogNumberDigits - количество цифр у максимально возможного номера лога. Нужно для выравнивания номера логов
 */
@Immutable
internal data class LogViewerViewState(
    val searchIndex: List<Int>,
    val logsViewState: LogsViewState,
    val searchState: SearchBarViewState,
    val isMappingApplied: Boolean,
    val showSelectMappingDialog: Boolean,
    val logRecordsAfterApplyFilter: Int,
) {
    companion object {
        val STUB = LogViewerViewState(
            searchIndex = emptyList(),
            logsViewState = LogsViewState.STUB,
            searchState = SearchBarViewState.STUB,
            isMappingApplied = false,
            showSelectMappingDialog = false,
            logRecordsAfterApplyFilter = 0,
        )
    }
}
