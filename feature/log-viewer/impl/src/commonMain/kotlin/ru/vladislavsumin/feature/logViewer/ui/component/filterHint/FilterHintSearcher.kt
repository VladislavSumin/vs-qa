package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import ru.vladislavsumin.core.searchUtils.SearchUtils

internal object FilterHintSearcher {
    fun search(hints: List<KeywordFilterHint>, search: String): List<FilterHintItem> = hints.parallelStream()
        .map { hint -> checkHint(hint, search) }
        .filter { it != null }
        .sorted { result, result1 -> -result!!.score.compareTo(result1!!.score) }
        .map { result ->
            FilterHintItem(
                text = result!!.hint.name,
                hint = result.hint.hint,
                searchLength = search.length,
                highlights = result.highlights,
            )
        }.toList() as List<FilterHintItem>

    private fun checkHint(hint: KeywordFilterHint, search: String): MatchResult? = startWithSearch(hint, search)
        ?: containsSearch(hint, search)
        ?: subsequenceSearch(hint, search, ignoreCase = false)
        ?: subsequenceSearch(hint, search, ignoreCase = true)

    private fun startWithSearch(hint: KeywordFilterHint, search: String): MatchResult? =
        if (hint.name.startsWith(search, ignoreCase = true)) {
            MatchResult(
                hint,
                highlights = listOf(search.indices),
                score = 5,
            )
        } else {
            null
        }

    private fun containsSearch(hint: KeywordFilterHint, search: String): MatchResult? {
        val index = hint.name.indexOf(search, ignoreCase = true)
        return if (index >= 0) {
            MatchResult(
                hint,
                highlights = listOf(index..<(index + search.length)),
                score = 4,
            )
        } else {
            null
        }
    }

    private fun subsequenceSearch(hint: KeywordFilterHint, search: String, ignoreCase: Boolean): MatchResult? {
        val result = SearchUtils.subsequenceSearch(hint.name, search, ignoreCase)
        return if (result != null) {
            MatchResult(
                hint,
                highlights = result,
                score = if (ignoreCase) 2 else 3,
            )
        } else {
            null
        }
    }

    private data class MatchResult(val hint: KeywordFilterHint, val highlights: List<IntRange>, val score: Int,)
}
