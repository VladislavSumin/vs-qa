package ru.vladislavsumin.feature.logViewer.ui.component.savedFilters

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.coroutines.utils.combine
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

    private val saveError = MutableStateFlow<String?>(null)

    val savedFilters: StateFlow<List<SavedFiltersRepository.SavedFilter>> =
        savedFiltersRepository.observeSavedFilters().stateIn(emptyList())

    private val filterRequestParser = FilterRequestParser(savedFilters)

    val state = combine(
        saveNewFilterName,
        saveNewFilterContent,
        savedFilters,
        editingFilterName,
        editName,
        editContent,
        saveError,
    ) { saveNewFilterName, saveNewFilterContent, savedFilters, editingFilterName, editName, editContent, saveError ->
        SavedFiltersViewState(
            saveNewFilterName = saveNewFilterName,
            saveNewFilterContent = saveNewFilterContent,
            savedFilters = savedFilters,
            editingFilterName = editingFilterName,
            editName = editName,
            editContent = editContent,
            saveError = saveError,
        )
    }
        .stateIn(initialValue = SavedFiltersViewState.STUB)

    fun onSavedFilterNameChanged(name: String) {
        saveNewFilterName.value = name
        saveError.value = null
    }

    fun onSavedFilterContentChanged(content: String) {
        saveNewFilterContent.value = content
        saveError.value = null
    }

    fun onClickSaveNewFilter() = launch {
        val error = validateNewFilter(saveNewFilterName.value, saveNewFilterContent.value)
        if (error != null) {
            saveError.value = error
            return@launch
        }
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
        saveError.value = null
        editingFilterName.value = filter.name
        editName.value = filter.name
        editContent.value = filter.content
    }

    fun onEditingFilterNameChanged(name: String) {
        editName.value = name
        saveError.value = null
    }

    fun onEditingFilterContentChanged(content: String) {
        editContent.value = content
        saveError.value = null
    }

    fun onCancelEditingFilter() {
        saveError.value = null
        editingFilterName.value = null
    }

    fun onClickSaveEditedFilter(filter: SavedFiltersRepository.SavedFilter) = launch {
        val error = validateEditedFilter(filter.name, editName.value, editContent.value)
        if (error != null) {
            saveError.value = error
            return@launch
        }
        savedFiltersRepository.update(
            oldFilter = filter,
            newName = editName.value,
            newContent = editContent.value,
        )
        editingFilterName.value = null
    }

    fun highlightSavedFilter(filter: SavedFiltersRepository.SavedFilter): FilterRequestParser.RequestHighlight =
        filterRequestParser.justHighlight(filter.content)

    private fun validateNewFilter(name: String, content: String): String? {
        val basic = validateNameAndContent(name, content)
        if (basic != null) return basic
        if (savedFilters.value.any { it.name == name }) {
            return "Filter with name \"$name\" already exists"
        }
        return null
    }

    private fun validateEditedFilter(oldName: String, newName: String, newContent: String): String? {
        val basic = validateNameAndContent(newName, newContent)
        if (basic != null) return basic
        if (newName != oldName && savedFilters.value.any { it.name == newName }) {
            return "Filter with name \"$newName\" already exists"
        }
        return null
    }

    private fun validateNameAndContent(name: String, content: String): String? = when {
        name.isBlank() -> "Filter name is required"

        content.isBlank() -> "Filter content is required"

        name.any {
            !it.isLetterOrDigit() && it != '_'
        } -> "Filter name may only contain letters, digits and underscores"

        name in filterRequestParser.reservedKeywords -> "Name \"$name\" conflicts with a reserved keyword"

        else -> null
    }
}
