package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.coroutines.utils.combine
import ru.vladislavsumin.core.decompose.components.ViewModel
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

    private val filter = MutableStateFlow(TextFieldValue())
    private val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))
    private val selectedSearchIndex = MutableStateFlow(0)
    private val visibleIndexes = MutableStateFlow(Pair(0, 0))

    private val logsInteractor = LogsInteractorImpl(
        scope = viewModelScope,
        logPath = logPath,
        proguardInteractor = mappingPath?.let { ProguardInteractorImpl(it) },
    )

    private val filterState = filter.map { filter ->
        filterRequestParser.tokenize(filter.text)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val state = combine(
        logsInteractor.observeLogIndex(
            filter = filterState.mapNotNull { it.searchRequest.getOrNull() },
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
        filterState.map { it.requestHighlight },
        search,
        selectedSearchIndex,
        logsInteractor.observeTotalRecords(),
    ) { logIndexProgress, filter, highlight, search, selectedSearchIndex, totalRecords ->
        LogViewerViewState(
            filterField = filter,
            filter = highlight,
            isFilterValid = highlight is FilterRequestParser.RequestHighlight.Success,
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
                filterField = TextFieldValue(),
                filter = FilterRequestParser.RequestHighlight.InvalidSyntax(""),
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

    fun onFilterChange(newValue: TextFieldValue) {
        filter.value = newValue
    }

    fun onSearchChange(newValue: String) = search.update { it.copy(search = newValue) }
    fun onClickSearchMatchCase(newValue: Boolean) = search.update { it.copy(matchCase = newValue) }
    fun onClickSearchUseRegex(newValue: Boolean) = search.update { it.copy(useRegex = newValue) }
}
