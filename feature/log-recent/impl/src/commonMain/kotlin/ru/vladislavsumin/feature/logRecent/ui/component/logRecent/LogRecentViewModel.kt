package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractorInternal

@GenerateFactory
@Stable
internal class LogRecentViewModel(
    private val logRecentInteractor: LogRecentInteractorInternal,
) : ViewModel() {
    val state = logRecentInteractor.observeRecents()
        .map { recents -> LogRecentViewState(recents) }
        .stateIn(LogRecentViewState.STUB)
}
