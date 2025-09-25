package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.runtime.Stable
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.coroutines.utils.combine
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractor
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractorImpl
import ru.vladislavsumin.feature.logViewer.domain.logs.SearchRequest
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractorImpl
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarUiInteractor
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsEvents
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsViewState
import ru.vladislavsumin.feature.logViewer.ui.component.searchBar.SearchBarViewState
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

@Stable
@GenerateFactory
internal class LogViewerViewModel(
    logParserProvider: LogParserProvider,
    private val windowTitleInteractor: WindowTitleInteractor,
    @ByCreate logPath: Path,
    @ByCreate mappingPath: Path?,
    @ByCreate private val bottomBarUiInteractor: BottomBarUiInteractor,
    @ByCreate private val filterBarUiInteractor: FilterBarUiInteractor,
) : NavigationViewModel() {
    private val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))
    private val selectedSearchIndex = MutableStateFlow(0)
    private val showSelectMappingDialog = MutableStateFlow(false)
    private val showDragAndDropContainers = MutableStateFlow(false)

    private val logsInteractor = LogsInteractorImpl(
        scope = viewModelScope,
        logPath = logPath,
        logParserProvider = logParserProvider,
        proguardInteractor = mappingPath?.let { ProguardInteractorImpl(it) },
    )

    val state = combine(
        logsInteractor.observeLogIndex(
            filter = filterBarUiInteractor.filterState.mapNotNull { it.searchRequest.getOrNull() },
            search = search,
        )
            .onEach {
                // TODO убрать эту жесть.
                selectedSearchIndex.value = 0
                if (it.lastSuccessIndex.searchIndex.index.isNotEmpty()) {
                    scrollToIndex(it.lastSuccessIndex.searchIndex.index[selectedSearchIndex.value])
                }
            },
        search,
        selectedSearchIndex,
        logsInteractor.observeMappingStatus(),
        showSelectMappingDialog,
        showDragAndDropContainers,
    ) {
            logIndexProgress, search, selectedSearchIndex, mappingStatus,
            showSelectMappingDialog, showDragAndDropContainers,
        ->

        val runIdOrders = logIndexProgress.lastSuccessIndex.runIdOrders
        val logsWithRunNumber = if (runIdOrders == null) {
            listOf(logIndexProgress.lastSuccessIndex.logs)
        } else {
            val logIterator = logIndexProgress.lastSuccessIndex.logs.listIterator()
            runIdOrders.map { info ->
                val items = mutableListOf<LogRecord>()
                while (logIterator.hasNext()) {
                    val item = logIterator.next()
                    if (item.order <= info.orderRange.last) {
                        items.add(item)
                    } else {
                        logIterator.previous()
                        break
                    }
                }
                items
            }
        }

        LogViewerViewState(
            searchIndex = logIndexProgress.lastSuccessIndex.searchIndex.index,
            logsViewState = LogsViewState(
                logs = logsWithRunNumber,
                rawLogs = logIndexProgress.lastSuccessIndex.logs,
                maxLogNumberDigits = (logIndexProgress.lastSuccessIndex.totalLogRecords + 1).toString().length,
            ),
            searchState = SearchBarViewState(
                searchRequest = search.search,
                isMatchCase = search.matchCase,
                isRegex = search.useRegex,
                isBadRegex = !logIndexProgress.isSearchingNow &&
                    logIndexProgress.lastSuccessIndex.searchIndex is LogIndex.SearchIndex.BadRegex,
                currentSearchResultIndex = selectedSearchIndex,
                totalSearchResults = logIndexProgress.lastSuccessIndex.searchIndex.index.size,
            ),
            isMappingApplied = when (mappingStatus) {
                LogsInteractor.MappingStatus.Attached -> true
                LogsInteractor.MappingStatus.NotAttached -> false
            },
            showSelectMappingDialog = showSelectMappingDialog,
            showDragAndDropContainers = showDragAndDropContainers,
            logRecordsAfterApplyFilter = logIndexProgress.lastSuccessIndex.logs.size,
        )
    }
        .stateIn(LogViewerViewState.STUB)

    val events = Channel<LogsEvents>()

    init {
        launch {
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
        launch {
            state
                .resubscribeOnUiLifecycle(Lifecycle.State.RESUMED)
                .collect { state ->
                    bottomBarUiInteractor.setBottomBarText("Total records: ${state.logRecordsAfterApplyFilter}")
                }
        }
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            windowTitleInteractor.setWindowTitleExtension(logPath.name)
        }
    }

    private fun scrollToIndex(index: Int) = launch {
        events.send(LogsEvents.ScrollToIndex(index))
    }

    fun onClickMappingButton() = launch {
        if (state.value.isMappingApplied) {
            logsInteractor.detachMapping()
        } else {
            showSelectMappingDialog.value = true
        }
    }

    fun onSelectMappingDialogResult(result: Path?) = launch {
        showSelectMappingDialog.value = false
        if (result != null) {
            logsInteractor.attachMapping(result)
        }
    }

    fun onDragAndDropLogsFile(file: File) {
        open(LogViewerScreenParams(logPath = file.toPath()))
    }

    fun onDragAndDropMappingFile(file: File) = launch {
        logsInteractor.attachMapping(file.toPath())
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

    fun onStartDragAndDrop() {
        showDragAndDropContainers.value = true
    }

    fun onStopDragAndDrop() {
        showDragAndDropContainers.value = false
    }

    fun onSearchChange(newValue: String) = search.update { it.copy(search = newValue) }
    fun onClickSearchMatchCase(newValue: Boolean) = search.update { it.copy(matchCase = newValue) }
    fun onClickSearchUseRegex(newValue: Boolean) = search.update { it.copy(useRegex = newValue) }
}
