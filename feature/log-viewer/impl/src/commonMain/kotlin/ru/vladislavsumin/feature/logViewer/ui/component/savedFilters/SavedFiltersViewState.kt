package ru.vladislavsumin.feature.logViewer.ui.component.savedFilters

import androidx.compose.runtime.Stable
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository

@Stable
internal data class SavedFiltersViewState(
    val saveNewFilterName: String,
    val saveNewFilterContent: String,
    val savedFilters: List<SavedFiltersRepository.SavedFilter>,
    val editingFilterName: String?,
    val editName: String,
    val editContent: String,
    val saveError: String?,
) {
    companion object {
        val STUB = SavedFiltersViewState(
            saveNewFilterName = "",
            saveNewFilterContent = "",
            savedFilters = emptyList(),
            editingFilterName = null,
            editName = "",
            editContent = "",
            saveError = null,
        )
    }
}
