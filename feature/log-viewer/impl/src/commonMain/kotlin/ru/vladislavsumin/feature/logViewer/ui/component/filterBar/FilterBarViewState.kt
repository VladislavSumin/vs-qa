package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
internal data class FilterBarViewState(
    val field: TextFieldValue,
    val predictionWordLength: Int,
    val highlight: FilterRequestParser.RequestHighlight,
    val error: String?,
    val showHelpMenu: Boolean,
    val showSavedFilters: Boolean,
) {

    companion object {
        val STUB = FilterBarViewState(
            field = TextFieldValue(),
            predictionWordLength = 0,
            highlight = FilterRequestParser.RequestHighlight.InvalidSyntax(""),
            error = null,
            showHelpMenu = false,
            showSavedFilters = false,
        )
    }
}
