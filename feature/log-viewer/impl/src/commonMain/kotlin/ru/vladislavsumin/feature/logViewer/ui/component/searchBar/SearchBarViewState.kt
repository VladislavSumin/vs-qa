package ru.vladislavsumin.feature.logViewer.ui.component.searchBar

internal data class SearchBarViewState(
    val searchRequest: String,

    val isMatchCase: Boolean,
    val isRegex: Boolean,
    val isBadRegex: Boolean,

    val currentSearchResultIndex: Int,
    val totalSearchResults: Int,
) {
    companion object Companion {
        val STUB = SearchBarViewState(
            searchRequest = "",
            isMatchCase = false,
            isRegex = false,
            isBadRegex = false,
            currentSearchResultIndex = 0,
            totalSearchResults = 0,
        )
    }
}
