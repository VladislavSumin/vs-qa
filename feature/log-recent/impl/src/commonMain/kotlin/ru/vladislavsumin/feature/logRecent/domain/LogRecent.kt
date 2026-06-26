package ru.vladislavsumin.feature.logRecent.domain

import java.nio.file.Path
import java.time.Instant

internal data class LogRecent(
    val id: Long,
    val path: Path,
    val mappingPath: Path?,
    val lastOpenTime: Instant,
    val searchRequest: String,
    val filterRequest: String,
    val selectedSearchIndex: Int,
    val scrollPosition: Int,
    val customName: String? = null,
)
