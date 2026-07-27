package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository

@GenerateFactory
@Stable
internal class FilterBarViewModel(@ByCreate savedFilters: StateFlow<List<SavedFiltersRepository.SavedFilter>>) :
    ViewModel(),
    FilterBarUiInteractor {
    private val filter = MutableStateFlow(TextFieldValue())

    private val showHelpMenu = MutableStateFlow(false)

    private val showSavedFilters = MutableStateFlow(false)

    private val filterRequestParser = FilterRequestParser(savedFilters)

    override val filterState: SharedFlow<FilterRequestParser.ParserResult> = filter.map { filter ->
        filterRequestParser.parse(request = filter.text, cursorPosition = filter.selection.start)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val state = combine(
        filter,
        filterState,
        showHelpMenu,
        showSavedFilters,
    ) { filter, filterState, showHelpMenu, showSavedFilters ->
        FilterBarViewState(
            field = filter,
            predictionWordLength = filterState.currentTokenPredictionInfo?.startText?.length ?: 0,
            highlight = filterState.requestHighlight,
            error = filterState.searchRequest.exceptionOrNull()?.let { it.message ?: "No error message provided" },
            showHelpMenu = showHelpMenu,
            showSavedFilters = showSavedFilters,
        )
    }
        .stateIn(initialValue = FilterBarViewState.STUB)

    val events = Channel<FilterBarEvent>()

    override fun setFilter(data: String) {
        filter.update { it.copy(text = data) }
    }

    override fun addToFilter(part: String) {
        filter.update { textFieldValue ->
            val currentText = textFieldValue.text
            val newText = if (currentText.isBlank()) part else "$currentText $part"
            textFieldValue.copy(text = newText)
        }
    }

    fun onFilterChange(newValue: TextFieldValue) {
        filter.update { old ->
            if (old.text.length < newValue.text.length) {
                events.trySend(FilterBarEvent.RequestShowHint)
            }
            newValue
        }
    }

    fun replaceFilterText(removeLen: Int, text: String) {
        filter.update { old ->
            val newText = old.text.substring(0, old.selection.start - removeLen) + text + old.text.substring(
                (old.selection.end).coerceAtMost(old.text.length),
                old.text.length,
            )

            old.copy(
                text = newText,
                selection = TextRange(
                    start = old.selection.start + text.length - removeLen,
                    end = old.selection.end + text.length - removeLen,
                ),
            )
        }
    }

    fun onClickSavedFilters() {
        showSavedFilters.update { !it }
    }

    fun onClickHelpButton() {
        showHelpMenu.update { !it }
    }

    fun onDismissHelpMenu() {
        showHelpMenu.value = false
    }
}
