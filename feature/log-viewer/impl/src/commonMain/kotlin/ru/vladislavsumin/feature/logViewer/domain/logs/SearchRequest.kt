package ru.vladislavsumin.feature.logViewer.domain.logs

data class SearchRequest(
    val search: String,
    val matchCase: Boolean,
    val useRegex: Boolean,
)
