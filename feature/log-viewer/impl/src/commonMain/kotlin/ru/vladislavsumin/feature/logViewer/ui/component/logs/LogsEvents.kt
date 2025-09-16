package ru.vladislavsumin.feature.logViewer.ui.component.logs

internal sealed interface LogsEvents {
    data class ScrollToIndex(val index: Int) : LogsEvents
}
