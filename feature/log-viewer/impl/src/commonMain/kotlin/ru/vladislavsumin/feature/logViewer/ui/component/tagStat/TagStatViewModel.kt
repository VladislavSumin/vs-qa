package ru.vladislavsumin.feature.logViewer.ui.component.tagStat

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord

@Stable
internal class TagStatViewModel(
    logs: Flow<List<LogRecord>>,
) : ViewModel() {
    val state = logs
        .map { logRecords ->
            // TODO излишне сложная цепочка с кучей лишних коллекций, оптимизировать.
            val tags = logRecords
                .groupBy { it.raw.substring(it.tag) }
                .map { (tag, records) ->
                    val totalSize = records.size
                    val levels = mutableMapOf<LogLevel, Int>()
                    records.forEach { record ->
                        levels[record.logLevel] = (levels[record.logLevel] ?: 0) + 1
                    }
                    val levelsList = levels.toList().sortedByDescending { it.first.rawLevel }
                    TagStatViewState.TagStatInfo(tag = tag, recordCount = totalSize, levels = levelsList)
                }
                .sortedByDescending { it.recordCount }
            TagStatViewState(tags)
        }
        .stateIn(
            initialValue = TagStatViewState.STUB,
            // Вкладка закрывается, поэтому обновляем данные только если контент визуально виден.
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
        )
}
