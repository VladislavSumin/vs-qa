package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel

internal class FilterBarViewModel : ViewModel(), FilterBarUiInteractor {
    private val filterRequestParser = FilterRequestParser()
    private val filter = MutableStateFlow(TextFieldValue())
    private val showHelpMenu = MutableStateFlow(false)

    override val filterState: SharedFlow<FilterRequestParser.ParserResult> = filter.map { filter ->
        filterRequestParser.tokenize(filter.text)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val state = combine(
        filter,
        filterState,
        showHelpMenu,
    ) { filter, filterState, showHelpMenu ->
        FilterBarViewState(
            field = filter,
            highlight = filterState.requestHighlight,
            isError = !filterState.searchRequest.isSuccess,
            showHelpMenu = showHelpMenu,
        )
    }
        .stateIn(
            initialValue = FilterBarViewState(
                field = filter.value,
                highlight = FilterRequestParser.RequestHighlight.InvalidSyntax(""),
                isError = false,
                showHelpMenu = false,
            ),
        )

    fun onFilterChange(newValue: TextFieldValue) {
        filter.value = newValue
    }

    fun onClickHelpButton() {
        showHelpMenu.update { !it }
    }

    fun onDismissHelpMenu() {
        showHelpMenu.value = false
    }
}
