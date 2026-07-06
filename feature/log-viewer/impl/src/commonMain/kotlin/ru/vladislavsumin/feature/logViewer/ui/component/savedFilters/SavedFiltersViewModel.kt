package ru.vladislavsumin.feature.logViewer.ui.component.savedFilters

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterRequestParser

@GenerateFactory
internal class SavedFiltersViewModel(private val savedFiltersRepository: SavedFiltersRepository) : ViewModel() {
    private val saveNewFilterName = MutableStateFlow("")
    private val saveNewFilterContent = MutableStateFlow("")

    private val editingFilterName = MutableStateFlow<String?>(null)
    private val editName = MutableStateFlow("")
    private val editContent = MutableStateFlow("")

    val savedFilters: StateFlow<List<SavedFiltersRepository.SavedFilter>> =
        savedFiltersRepository.observeSavedFilters().stateIn(emptyList())

    private val filterRequestParser = FilterRequestParser(savedFilters)

    private val editingState = combine(
        editingFilterName,
        editName,
        editContent,
    ) { editingFilterName, editName, editContent ->
        Triple(editingFilterName, editName, editContent)
    }

    val state = combine(
        saveNewFilterName,
        saveNewFilterContent,
        savedFilters,
        editingState,
    ) { saveNewFilterName, saveNewFilterContent, savedFilters, editingState ->
        val (editingFilterName, editName, editContent) = editingState
        SavedFiltersViewState(
            saveNewFilterName = saveNewFilterName,
            saveNewFilterContent = saveNewFilterContent,
            savedFilters = savedFilters,
            editingFilterName = editingFilterName,
            editName = editName,
            editContent = editContent,
        )
    }
        .stateIn(initialValue = SavedFiltersViewState.STUB)

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

    fun highlightSavedFilter(filter: SavedFiltersRepository.SavedFilter): FilterRequestParser.RequestHighlight =
        filterRequestParser.justHighlight(filter.content)
}
