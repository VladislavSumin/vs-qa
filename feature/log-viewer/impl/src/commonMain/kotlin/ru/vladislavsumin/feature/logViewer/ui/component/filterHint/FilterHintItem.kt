package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import ru.vladislavsumin.feature.logParser.domain.LogLevel

internal data class FilterHintItem(
    val text: String,
    val hint: String?,
    val key: String = text,
    val selectedPartLength: Int,
)

internal data class KeywordFilterHint(
    val name: String,
    val hint: String? = null,
)

internal val keywordFilterHintItems = listOf(
    KeywordFilterHint(name = "tag", hint = "Filter by log tag"),
    KeywordFilterHint(name = "pid", hint = "Filter by process ID"),
    KeywordFilterHint(name = "tid", hint = "Filter by thread ID"),
    KeywordFilterHint(name = "thread", hint = "Filter by thread name"),
    KeywordFilterHint(name = "message", hint = "Filter by message"),
    KeywordFilterHint(name = "level", hint = "Filter by minimal log level"),
    KeywordFilterHint(name = "runNumber", hint = "Filter by run number"),
    KeywordFilterHint(name = "timeAfter", hint = "Filter by time after"),
    KeywordFilterHint(name = "timeBefore", hint = "Filter by time before"),
)

internal val typeFilterHintItems = listOf(
    KeywordFilterHint(name = "=", hint = "Contains"),
    KeywordFilterHint(name = ":=", hint = "Exactly"),
)

internal val logLevelFilterHintItems = LogLevel.entries.map { level ->
    val aliases = level.aliases.filter { it != level.name }.joinToString(", ") { it.lowercase() }
    KeywordFilterHint(
        name = level.aliases.first().lowercase(),
        hint = "${level.name.lowercase()} ($aliases)",
    )
}
