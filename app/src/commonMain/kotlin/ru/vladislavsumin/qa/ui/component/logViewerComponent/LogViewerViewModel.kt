package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogsInteractorImpl
import java.nio.file.Path

@Stable
internal class LogViewerViewModel(
    logPath: Path,
) : ViewModel() {
    private val filter = MutableStateFlow("")
    private val search = MutableStateFlow("")
    private val selectedSearchIndex = MutableStateFlow(0)
    private val isFilterUseRegex = MutableStateFlow(false)
    private val isSearchUseRegex = MutableStateFlow(false)
    private val visibleIndexes = MutableStateFlow(Pair(0, 0))

    private val logsInteractor = LogsInteractorImpl(logPath)

    val state = combine(
        logsInteractor.observeLogIndex(filter, search)
            .onEach {
                // TODO убрать эту жесть.
                selectedSearchIndex.value = 0
                if (it.lastSuccessIndex.searchIndex.index.isNotEmpty()) {
                    scrollToIndex(it.lastSuccessIndex.searchIndex.index[selectedSearchIndex.value])
                }
            },
        filter,
        search,
        isFilterUseRegex,
        isSearchUseRegex,
        selectedSearchIndex,
    ) { logIndexProgress, filter, search, isFilterUseRegex, isSearchUseRegex, selectedSearchIndex ->
        LogViewerViewState(
            filter = filter,
            search = search,
            isFilterUseRegex = isFilterUseRegex,
            isSearchUseRegex = isSearchUseRegex,
            searchResults = logIndexProgress.lastSuccessIndex.searchIndex.index.size,
            selectedSearchIndex = selectedSearchIndex,
            searchIndex = logIndexProgress.lastSuccessIndex.searchIndex.index,
            logs = logIndexProgress.lastSuccessIndex.logs,
            maxLogNumberDigits = logsInteractor.logs.last().order.toString().length,
        )
    }
        .stateIn(
            LogViewerViewState(
                filter = "",
                search = "",
                isFilterUseRegex = false,
                isSearchUseRegex = false,
                searchResults = 0,
                selectedSearchIndex = 0,
                searchIndex = emptyList(),
                logs = emptyList(),
                maxLogNumberDigits = 0,
            ),
        )

    val events = Channel<LogViewerEvents>()

    private fun scrollToIndex(index: Int) {
        // TODO котсылина временная
        launch(Dispatchers.Main) {
            events.send(LogViewerEvents.ScrollToIndex(index))
        }
    }

    fun onClickPrevIndex() {
        if (state.value.searchIndex.isNotEmpty()) {
            selectedSearchIndex.value = (selectedSearchIndex.value - 1).coerceAtLeast(0)
            scrollToIndex(state.value.searchIndex[selectedSearchIndex.value])
        }
    }

    fun onClickNextIndex() {
        if (state.value.searchIndex.isNotEmpty()) {
            selectedSearchIndex.value =
                (selectedSearchIndex.value + 1).coerceAtMost(state.value.searchIndex.size - 1)
            scrollToIndex(state.value.searchIndex[selectedSearchIndex.value])
        }
    }

    fun onVisibleItemsChanged(firstIndex: Int, lastIndex: Int) {
        visibleIndexes.value = firstIndex to lastIndex
    }

    fun onFilterChange(newValue: String) {
        filter.value = newValue
    }

    fun onSearchChange(newValue: String) {
        search.value = newValue
    }

    fun onClickFilterUseRegex(newValue: Boolean) {
        isFilterUseRegex.value = newValue
    }

    fun onClickSearchUseRegex(newValue: Boolean) {
        isFilterUseRegex.value = newValue
    }
}

// TODO и эту жесть убрать
@Suppress("MagicNumber")
private fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
    )
}
