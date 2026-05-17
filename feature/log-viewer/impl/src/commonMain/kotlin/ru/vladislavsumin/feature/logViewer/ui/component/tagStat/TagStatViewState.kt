package ru.vladislavsumin.feature.logViewer.ui.component.tagStat

import ru.vladislavsumin.feature.logParser.domain.LogLevel

internal data class TagStatViewState(
    val tags: List<TagStatInfo>,
) {
    data class TagStatInfo(
        val tag: String,
        val recordCount: Int,
        val levels: List<Pair<LogLevel, Int>>,
    )

    companion object {
        val STUB = TagStatViewState(
            tags = emptyList(),
        )
    }
}
