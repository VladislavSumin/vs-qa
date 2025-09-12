package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.feature.logViewer.domain.logs.FilterRequest
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractor
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractorImpl
import ru.vladislavsumin.feature.logViewer.domain.logs.SearchRequest
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractorImpl
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.filterBar.FilterRequestParser
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.searchBar.LogSearchBarViewState
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import java.nio.file.Path
import kotlin.map

@Stable
internal class LogViewerViewModel(
    logPath: Path,
    mappingPath: Path?,
    private val bottomBarUiInteractor: BottomBarUiInteractor,
) : ViewModel() {
    private val filterRequestParser = FilterRequestParser()

    private val filter = MutableStateFlow("")
    private val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))
    private val selectedSearchIndex = MutableStateFlow(0)
    private val visibleIndexes = MutableStateFlow(Pair(0, 0))

    private val logsInteractor = LogsInteractorImpl(
        scope = viewModelScope,
        logPath = logPath,
        proguardInteractor = mappingPath?.let { ProguardInteractorImpl(it) },
    )

    private val filterState = filter.map { filter ->
        filterRequestParser.tokenize(filter)
            .map { FilterState.Valid(it) }
            .getOrElse { FilterState.Invalid }
    }

    val state = combine(
        logsInteractor.observeLogIndex(
            filter = filterState.filterIsInstance<FilterState.Valid>().map { it.request },
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
        filterState.map { it is FilterState.Valid },
        search,
        selectedSearchIndex,
        logsInteractor.observeTotalRecords(),
    ) { logIndexProgress, filter, isFilterValid, search, selectedSearchIndex, totalRecords ->
        LogViewerViewState(
            filter = filter,
            isFilterValid = isFilterValid,
            searchIndex = logIndexProgress.lastSuccessIndex.searchIndex.index,
            logs = logIndexProgress.lastSuccessIndex.logs,
            maxLogNumberDigits = totalRecords.toString().length,
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
        .onEach { state ->
            bottomBarUiInteractor.setBottomBarText("Total records: ${state.logs.size}")
        }
        .stateIn(
            LogViewerViewState(
                filter = "",
                isFilterValid = true,
                searchIndex = emptyList(),
                logs = emptyList(),
                maxLogNumberDigits = 0,
                searchState = LogSearchBarViewState.STUB,
            ),
        )

    val events = Channel<LogViewerEvents>()

    init {
        viewModelScope.launch {
            logsInteractor.observeLoadingStatus().collectLatest {
                when (it) {
                    LogsInteractor.LoadingStatus.Loaded -> Unit
                    LogsInteractor.LoadingStatus.LoadingLogs -> {
                        bottomBarUiInteractor.showProgressBar("Loading logs")
                    }

                    LogsInteractor.LoadingStatus.DeobfuscateLogs -> {
                        bottomBarUiInteractor.showProgressBar("Deobfuscate logs")
                    }
                }
            }
        }
    }

    private fun scrollToIndex(index: Int) {
        // TODO котсылина временная
        launch(Dispatchers.Main) {
            events.send(LogViewerEvents.ScrollToIndex(index))
        }
    }

    fun onClickPrevIndex() {
        if (state.value.searchIndex.isNotEmpty()) {
            if (selectedSearchIndex.value == 0) {
                selectedSearchIndex.value = state.value.searchIndex.size - 1
            } else {
                selectedSearchIndex.value -= 1
            }
            scrollToIndex(state.value.searchIndex[selectedSearchIndex.value])
        }
    }

    fun onClickNextIndex() {
        if (state.value.searchIndex.isNotEmpty()) {
            if (selectedSearchIndex.value == state.value.searchIndex.size - 1) {
                selectedSearchIndex.value = 0
            } else {
                selectedSearchIndex.value += 1
            }
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

    sealed interface FilterState {
        data object Invalid : FilterState
        data class Valid(val request: FilterRequest) : FilterState
    }
}

// TODO сделать под это дело отдельный модуль
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
