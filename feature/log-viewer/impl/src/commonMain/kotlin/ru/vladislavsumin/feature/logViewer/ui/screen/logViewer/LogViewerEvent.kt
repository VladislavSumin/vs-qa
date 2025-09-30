package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

internal sealed interface LogViewerEvent {
    data object FocusSearch : LogViewerEvent
}
