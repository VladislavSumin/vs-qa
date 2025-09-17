package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue

@Stable
data class FilterBarViewState(
    val field: TextFieldValue,
    val highlight: FilterRequestParser.RequestHighlight,
    val isError: Boolean,
    val showHelpMenu: Boolean,
)
