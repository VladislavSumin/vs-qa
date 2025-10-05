package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue

@Stable
internal data class FilterBarViewState(
    val field: TextFieldValue,
    val highlight: FilterRequestParser.RequestHighlight,
    val error: String?,
    val showHelpMenu: Boolean,
    val savedFiltersState: SavedFiltersState,
) {

    data class SavedFiltersState(
        val showSavedFilters: Boolean,
        val saveNewFilterName: String,
        val saveNewFilterContent: String,
        val savedFilters: List<SavedFilter>,
    ) {
        data class SavedFilter(
            val name: String,
            val content: String,
        )
    }

    companion object {
        val STUB = FilterBarViewState(
            field = TextFieldValue(),
            highlight = FilterRequestParser.RequestHighlight.InvalidSyntax(""),
            error = null,
            showHelpMenu = false,
            savedFiltersState = SavedFiltersState(
                showSavedFilters = false,
                saveNewFilterName = "",
                saveNewFilterContent = "",
                savedFilters = emptyList(),
            ),
        )
    }
}
