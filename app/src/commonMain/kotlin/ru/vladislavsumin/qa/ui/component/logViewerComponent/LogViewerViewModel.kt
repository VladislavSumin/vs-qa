package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogRecord
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
    private val internalState = combine(
        logsInteractor.observeLogIndex(filter, search),
        filter,
        search,
        isFilterUseRegex,
        isSearchUseRegex
    ) { logIndexProgress, filter, search, isFilterUseRegex, isSearchUseRegex ->
        // TODO весь код ниже это пока один большой костыль. Работает на честном слове.


        val filteredLogs = logIndexProgress.lastSuccessIndex.logs

        val searchResults = if (search.isEmpty()) 0 else {
            filteredLogs.count { it.searchHighlight != null }
        }

        val searchIndex: List<Pair<Int, LogRecord>> = if (search.isNotEmpty()) {
            filteredLogs.mapIndexedNotNull { index, record ->
                if (record.searchHighlight != null) index to record else null
            }
        } else {
            emptyList()
        }

        selectedSearchIndex.value = 0

        if (searchIndex.size > 0) {
            scrollToIndex(searchIndex[selectedSearchIndex.value].first)
        }

        IntermediateState(
            filter = filter,
            search = search,
            isFilterUseRegex = isFilterUseRegex,
            isSearchUseRegex = isSearchUseRegex,
            searchResults = searchResults,
            searchIndex = searchIndex,
            logs = filteredLogs,
            maxLogNumberDigits = logsInteractor.logs.last().order.toString().length,
        )
    }.stateIn(
        IntermediateState(
            filter = "",
            search = "",
            isFilterUseRegex = false,
            isSearchUseRegex = false,
            searchResults = 0,
            searchIndex = emptyList(),
            logs = emptyList(),
            maxLogNumberDigits = 0,
        )
    )

    val state = combine(
        internalState, selectedSearchIndex,
    ) { internalState, selectedSearchIndex ->
        LogViewerViewState(
            filter = internalState.filter,
            search = internalState.search,
            isFilterUseRegex = internalState.isFilterUseRegex,
            isSearchUseRegex = internalState.isSearchUseRegex,
            searchResults = internalState.searchResults,
            selectedSearchIndex = selectedSearchIndex,
            searchIndex = internalState.searchIndex,
            logs = internalState.logs,
            maxLogNumberDigits = internalState.maxLogNumberDigits,
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
            )
        )

    val events = Channel<LogViewerEvents>()

    private fun scrollToIndex(index: Int) {
        // TODO котсылина временная
        launch(Dispatchers.Main) {
            events.send(LogViewerEvents.ScrollToIndex(index))
        }
    }

    fun onClickPrevIndex() {
        if (internalState.value.searchIndex.isNotEmpty()) {
            selectedSearchIndex.value = (selectedSearchIndex.value - 1).coerceAtLeast(0)
            scrollToIndex(internalState.value.searchIndex[selectedSearchIndex.value].first)
        }
    }

    fun onClickNextIndex() {
        if (internalState.value.searchIndex.isNotEmpty()) {
            selectedSearchIndex.value =
                (selectedSearchIndex.value + 1).coerceAtMost(internalState.value.searchIndex.size - 1)
            scrollToIndex(internalState.value.searchIndex[selectedSearchIndex.value].first)
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

    private data class IntermediateState(
        val filter: String,
        val search: String,
        val isFilterUseRegex: Boolean,
        val isSearchUseRegex: Boolean,
        val searchResults: Int,
        val searchIndex: List<Pair<Int, LogRecord>>,
        val logs: List<LogRecord>,
        val maxLogNumberDigits: Int,
    )
}