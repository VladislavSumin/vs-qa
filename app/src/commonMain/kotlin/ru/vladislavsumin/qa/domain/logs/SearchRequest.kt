package ru.vladislavsumin.qa.domain.logs

data class SearchRequest(
    val search: String,
    val matchCase: Boolean,
    val useRegex: Boolean,
)
