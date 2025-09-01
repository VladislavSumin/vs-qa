package ru.vladislavsumin.qa.ui.component.logViewerComponent

sealed interface LogViewerEvents {
    data class ScrollToIndex(val index: Int) : LogViewerEvents
}
