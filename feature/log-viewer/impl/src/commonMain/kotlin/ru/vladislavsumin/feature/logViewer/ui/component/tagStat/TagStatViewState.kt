package ru.vladislavsumin.feature.logViewer.ui.component.tagStat

internal data class TagStatViewState(
    val tags: List<TagStatInfo>,
) {
    data class TagStatInfo(
        val tag: String,
        val recordCount: Int,
    )

    companion object {
        val STUB = TagStatViewState(
            tags = emptyList(),
        )
    }
}
