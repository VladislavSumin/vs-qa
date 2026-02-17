package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

internal sealed interface FilterHintViewState {
    data object Hidden : FilterHintViewState
    data class Show(
        val selectedItemKey: String,
        val items: List<FilterHintItem>,
    ) : FilterHintViewState
}
