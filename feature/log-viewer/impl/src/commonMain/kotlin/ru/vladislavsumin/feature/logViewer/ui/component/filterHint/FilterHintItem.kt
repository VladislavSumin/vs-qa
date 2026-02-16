package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

internal data class FilterHintItem(
    val text: String,
    val key: String = text,
    val selectedPartLength: Int,
)

internal data class KeywordFilterHint(
    val name: String,
)

internal val keywordFilterHintItems = listOf(
    KeywordFilterHint("tag"),
    KeywordFilterHint("pid"),
    KeywordFilterHint("tid"),
    KeywordFilterHint("thread"),
    KeywordFilterHint("message"),
    KeywordFilterHint("level"),
    KeywordFilterHint("runNumber"),
    KeywordFilterHint("timeAfter"),
    KeywordFilterHint("timeBefore"),
)
