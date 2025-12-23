package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import ru.vladislavsumin.feature.logRecent.domain.LogRecent

internal data class LogRecentViewState(
    val recents: List<LogRecent>,
) {
    companion object {
        val STUB = LogRecentViewState(
            recents = emptyList(),
        )
    }
}
