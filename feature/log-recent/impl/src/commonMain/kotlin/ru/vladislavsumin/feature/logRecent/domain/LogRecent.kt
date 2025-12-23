package ru.vladislavsumin.feature.logRecent.domain

import java.nio.file.Path
import java.time.Instant

internal data class LogRecent(
    val path: Path,
    val lastOpenTime: Instant,
)
