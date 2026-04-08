package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

internal object FilterHintSearcher {
    fun search(hints: List<KeywordFilterHint>, search: String): List<FilterHintItem> {
        return hints.parallelStream()
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
    }

    private fun checkHint(hint: KeywordFilterHint, search: String): MatchResult? {
        return startWithSearch(hint, search) ?: containsSearch(hint, search)
    }

    private fun startWithSearch(hint: KeywordFilterHint, search: String): MatchResult? {
        return if (hint.name.startsWith(search, ignoreCase = true)) {
            MatchResult(
                hint,
                highlights = listOf(IntRange(0, search.length)),
                score = 5,
            )
        } else {
            null
        }
    }

    private fun containsSearch(hint: KeywordFilterHint, search: String): MatchResult? {
        val index = hint.name.indexOf(search, ignoreCase = true)
        return if (index >= 0) {
            MatchResult(
                hint,
                highlights = listOf(IntRange(index, index + search.length)),
                score = 4,
            )
        } else {
            null
        }
    }

    private data class MatchResult(
        val hint: KeywordFilterHint,
        val highlights: List<IntRange>,
        val score: Int,
    )
}
