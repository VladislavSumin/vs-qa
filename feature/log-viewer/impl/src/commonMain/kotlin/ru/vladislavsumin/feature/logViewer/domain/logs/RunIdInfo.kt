package ru.vladislavsumin.feature.logViewer.domain.logs

data class RunIdInfo(
    val orderRange: IntRange,
    val meta: Map<String, String>,
)
