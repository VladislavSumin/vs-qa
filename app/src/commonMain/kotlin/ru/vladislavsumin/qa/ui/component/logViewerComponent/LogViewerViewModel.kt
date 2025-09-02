package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogIndex
import ru.vladislavsumin.qa.domain.logs.LogsInteractorImpl
import ru.vladislavsumin.qa.domain.logs.SearchRequest
import ru.vladislavsumin.qa.ui.component.logViewerComponent.searchBar.LogSearchBarViewState
import java.nio.file.Path

@Stable
internal class LogViewerViewModel(
    logPath: Path,
) : ViewModel() {
    private val filter = MutableStateFlow("")
    private val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))
    private val selectedSearchIndex = MutableStateFlow(0)
    private val isFilterUseRegex = MutableStateFlow(false)
    private val visibleIndexes = MutableStateFlow(Pair(0, 0))

    private val logsInteractor = LogsInteractorImpl(logPath)

    val state = combine(
        logsInteractor.observeLogIndex(
            filter = filter,
            search = search,
        )
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
        selectedSearchIndex,
    ) { logIndexProgress, filter, search, isFilterUseRegex, selectedSearchIndex ->
        LogViewerViewState(
            filter = filter,
            isFilterUseRegex = isFilterUseRegex,
            searchIndex = logIndexProgress.lastSuccessIndex.searchIndex.index,
            logs = logIndexProgress.lastSuccessIndex.logs,
            maxLogNumberDigits = logsInteractor.logs.last().order.toString().length,
            searchState = LogSearchBarViewState(
                searchRequest = search.search,
                isMatchCase = search.matchCase,
                isRegex = search.useRegex,
                isBadRegex = !logIndexProgress.isSearchingNow &&
                    logIndexProgress.lastSuccessIndex.searchIndex is LogIndex.SearchIndex.BadRegex,
                currentSearchResultIndex = selectedSearchIndex,
                totalSearchResults = logIndexProgress.lastSuccessIndex.searchIndex.index.size,
            ),
        )
    }
        .stateIn(
            LogViewerViewState(
                filter = "",
                isFilterUseRegex = false,
                searchIndex = emptyList(),
                logs = emptyList(),
                maxLogNumberDigits = 0,
                searchState = LogSearchBarViewState.STUB,
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

    fun onSearchChange(newValue: String) = search.update { it.copy(search = newValue) }
    fun onClickSearchMatchCase(newValue: Boolean) = search.update { it.copy(matchCase = newValue) }
    fun onClickSearchUseRegex(newValue: Boolean) = search.update { it.copy(useRegex = newValue) }

    fun onClickFilterUseRegex(newValue: Boolean) {
        isFilterUseRegex.value = newValue
    }
}
