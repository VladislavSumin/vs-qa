package ru.vladislavsumin.feature.logViewer.ui.component.tagStat

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.decompose.components.ViewModel
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
                .mapValues { it.value.size }
                .toList()
                .map { TagStatViewState.TagStatInfo(tag = it.first, recordCount = it.second) }
                .sortedByDescending { it.recordCount }
            TagStatViewState(tags)
        }
        .stateIn(
            initialValue = TagStatViewState.STUB,
            // Вкладка закрывается, поэтому обновляем данные только если контент визуально виден.
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
        )
}
