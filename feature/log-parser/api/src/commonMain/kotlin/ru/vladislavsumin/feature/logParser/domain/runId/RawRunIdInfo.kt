package ru.vladislavsumin.feature.logParser.domain.runId

data class RawRunIdInfo(
    val startIndex: Int,
    val meta: Map<String, String>,
) {
    constructor(startIndex: Int, vararg meta: Pair<String, String>) : this(startIndex, meta.toMap())
}
