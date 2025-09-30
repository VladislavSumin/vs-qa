package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

internal sealed interface FilterBarEvent {
    data object Focus : FilterBarEvent
}
