package ru.vladislavsumin.qa.ui.component.logViewerComponent.searchBar

internal data class LogSearchBarViewState(
    val searchRequest: String,

    val isMatchCase: Boolean,
    val isRegex: Boolean,

    val currentSearchResultIndex: Int,
    val totalSearchResults: Int,
) {
    companion object {
        val STUB = LogSearchBarViewState(
            searchRequest = "",
            isMatchCase = false,
            isRegex = false,
            currentSearchResultIndex = 0,
            totalSearchResults = 0,
        )
    }
}
