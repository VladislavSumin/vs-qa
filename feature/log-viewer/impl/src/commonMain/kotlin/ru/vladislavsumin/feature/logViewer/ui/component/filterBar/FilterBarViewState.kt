package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue

@Stable
internal data class FilterBarViewState(
    val field: TextFieldValue,
    val highlight: FilterRequestParser.RequestHighlight,
    val error: String?,
    val showHelpMenu: Boolean,
) {

    companion object {
        val STUB = FilterBarViewState(
            field = TextFieldValue(),
            highlight = FilterRequestParser.RequestHighlight.InvalidSyntax(""),
            error = null,
            showHelpMenu = false,
        )
    }
}
