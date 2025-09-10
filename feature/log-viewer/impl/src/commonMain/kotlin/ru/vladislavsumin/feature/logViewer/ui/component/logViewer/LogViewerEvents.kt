package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

sealed interface LogViewerEvents {
    data class ScrollToIndex(val index: Int) : LogViewerEvents
}
