package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.input.TextFieldValue
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.logViewer.domain.SavedFiltersRepository

@GenerateFactory
internal class FilterBarViewModel(
    private val globalHotkeyManager: GlobalHotkeyManager,
    private val savedFiltersRepository: SavedFiltersRepository,
) : ViewModel(), FilterBarUiInteractor {
    private val filter = MutableStateFlow(TextFieldValue())

    private val showHelpMenu = MutableStateFlow(false)

    private val showSavedFilters = MutableStateFlow(false)
    private val saveNewFilterName = MutableStateFlow("")
    private val saveNewFilterContent = MutableStateFlow("")

    private val savedFilters = savedFiltersRepository.observeSavedFilters()
        .stateIn(emptyList())

    private val filterRequestParser = FilterRequestParser(savedFilters)

    override val filterState: SharedFlow<FilterRequestParser.ParserResult> = filter.map { filter ->
        filterRequestParser.parse(request = filter.text, cursorPosition = filter.selection.start)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val savedFiltersState = combine(
        showSavedFilters,
        saveNewFilterName,
        saveNewFilterContent,
        savedFilters,
    ) { showSavedFilters, saveNewFilterName, saveNewFilterContent, savedFilters ->
        FilterBarViewState.SavedFiltersState(
            showSavedFilters = showSavedFilters,
            saveNewFilterName = saveNewFilterName,
            saveNewFilterContent = saveNewFilterContent,
            savedFilters = savedFilters,
        )
    }

    val state = combine(
        filter,
        filterState,
        showHelpMenu,
        savedFiltersState,
    ) { filter, filterState, showHelpMenu, savedFiltersState ->
        FilterBarViewState(
            field = filter,
            highlight = filterState.requestHighlight,
            error = filterState.searchRequest.exceptionOrNull()?.let { it.message ?: "No error message provided" },
            showHelpMenu = showHelpMenu,
            savedFiltersState = savedFiltersState,
        )
    }
        .stateIn(initialValue = FilterBarViewState.STUB)

    val events = Channel<FilterBarEvent>()

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            globalHotkeyManager.subscribe(
                KeyModifier.Command + KeyModifier.Shift + Key.F to {
                    events.trySend(FilterBarEvent.Focus)
                    true
                },
            )
        }
    }

    override fun setFilter(data: String) {
        filter.update { it.copy(text = data) }
    }

    fun onFilterChange(newValue: TextFieldValue) {
        filter.value = newValue
    }

    fun onClickSavedFilters() {
        showSavedFilters.update { !it }
    }

    fun onSavedFilterNameChanged(name: String) {
        saveNewFilterName.value = name
    }

    fun onSavedFilterContentChanged(content: String) {
        saveNewFilterContent.value = content
    }

    fun onClickSaveNewFilter() = launch {
        savedFiltersRepository.add(
            name = saveNewFilterName.value,
            content = saveNewFilterContent.value,
        )
        saveNewFilterName.value = ""
        saveNewFilterContent.value = ""
    }

    fun onDeleteSavedFilter(filter: SavedFiltersRepository.SavedFilter) = launch {
        savedFiltersRepository.remove(filter)
    }

    fun onClickHelpButton() {
        showHelpMenu.update { !it }
    }

    fun onDismissHelpMenu() {
        showHelpMenu.value = false
    }

    fun highlightSavedFilter(
        filter: SavedFiltersRepository.SavedFilter,
    ): FilterRequestParser.RequestHighlight {
        return filterRequestParser.justHighlight(filter.content)
    }
}
