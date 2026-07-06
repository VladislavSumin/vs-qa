package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository

@GenerateFactory
internal class FilterBarViewModel(private val savedFiltersRepository: SavedFiltersRepository) :
    ViewModel(),
    FilterBarUiInteractor {
    private val filter = MutableStateFlow(TextFieldValue())

    private val showHelpMenu = MutableStateFlow(false)

    private val showSavedFilters = MutableStateFlow(false)
    private val saveNewFilterName = MutableStateFlow("")
    private val saveNewFilterContent = MutableStateFlow("")

    private val editingFilterName = MutableStateFlow<String?>(null)
    private val editName = MutableStateFlow("")
    private val editContent = MutableStateFlow("")

    private val savedFilters = savedFiltersRepository.observeSavedFilters()
        .stateIn(emptyList())

    private val filterRequestParser = FilterRequestParser(savedFilters)

    override val filterState: SharedFlow<FilterRequestParser.ParserResult> = filter.map { filter ->
        filterRequestParser.parse(request = filter.text, cursorPosition = filter.selection.start)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val editingState = combine(
        editingFilterName,
        editName,
        editContent,
    ) { editingFilterName, editName, editContent ->
        Triple(editingFilterName, editName, editContent)
    }

    private val savedFiltersState = combine(
        showSavedFilters,
        saveNewFilterName,
        saveNewFilterContent,
        savedFilters,
        editingState,
    ) { showSavedFilters, saveNewFilterName, saveNewFilterContent, savedFilters, editingState ->
        val (editingFilterName, editName, editContent) = editingState
        FilterBarViewState.SavedFiltersState(
            showSavedFilters = showSavedFilters,
            saveNewFilterName = saveNewFilterName,
            saveNewFilterContent = saveNewFilterContent,
            savedFilters = savedFilters,
            editingFilterName = editingFilterName,
            editName = editName,
            editContent = editContent,
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
            predictionWordLength = filterState.currentTokenPredictionInfo?.startText?.length ?: 0,
            highlight = filterState.requestHighlight,
            error = filterState.searchRequest.exceptionOrNull()?.let { it.message ?: "No error message provided" },
            showHelpMenu = showHelpMenu,
            savedFiltersState = savedFiltersState,
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

    fun onStartEditingFilter(filter: SavedFiltersRepository.SavedFilter) {
        editingFilterName.value = filter.name
        editName.value = filter.name
        editContent.value = filter.content
    }

    fun onEditingFilterNameChanged(name: String) {
        editName.value = name
    }

    fun onEditingFilterContentChanged(content: String) {
        editContent.value = content
    }

    fun onCancelEditingFilter() {
        editingFilterName.value = null
    }

    fun onClickSaveEditedFilter(filter: SavedFiltersRepository.SavedFilter) = launch {
        savedFiltersRepository.update(
            oldFilter = filter,
            newName = editName.value,
            newContent = editContent.value,
        )
        editingFilterName.value = null
    }

    fun onClickHelpButton() {
        showHelpMenu.update { !it }
    }

    fun onDismissHelpMenu() {
        showHelpMenu.value = false
    }

    fun highlightSavedFilter(filter: SavedFiltersRepository.SavedFilter): FilterRequestParser.RequestHighlight =
        filterRequestParser.justHighlight(filter.content)
}
