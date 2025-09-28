package ru.vladislavsumin.feature.logViewer.domain.logs.delegates.filter

import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo

internal data class FilterLogProgress(
    val isFilteringNow: Boolean,
    val logs: List<LogRecord>,
    val totalLogRecords: Int,
    val runIdOrders: List<RunIdInfo>?,
)
