package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.runtime.Stable
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.coroutines.utils.LinkedFlow
import ru.vladislavsumin.core.coroutines.utils.combine
import ru.vladislavsumin.core.coroutines.utils.linkTo
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel
import ru.vladislavsumin.core.ui.resources.asResourceString
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.substring
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractor
import ru.vladislavsumin.feature.logViewer.domain.logs.LiveProcessNameEnricher
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogOrder
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractor
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractorImpl
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsSource
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.domain.logs.SearchRequest
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractorImpl
import ru.vladislavsumin.feature.logViewer.repository.LogViewerSettingsRepository
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarUiInteractor
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsEvents
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsViewState
import ru.vladislavsumin.feature.logViewer.ui.component.searchBar.SearchBarViewState
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.Res
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_bottom_deobfuscate
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_bottom_loading
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_bottom_total_records
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_mapping_expected_one
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.Notification
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport
import java.nio.file.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.name

@Stable
@GenerateFactory
internal class LogViewerViewModel(
    logParserProvider: LogParserProvider,
    private val logViewerSettingsRepository: LogViewerSettingsRepository,
    private val logRecentInteractor: LogRecentInteractor,
    private val dispatchers: VsDispatchers,
    adbClient: AdbClient,
    @ByCreate private val source: LogViewerSource,
    @ByCreate mappingPath: Path?,
    @ByCreate currentTags: LinkedFlow<Set<String>>,
    @ByCreate currentPackages: LinkedFlow<Set<String>>,
    @ByCreate currentRuns: LinkedFlow<List<RunIdInfo>>,
    @ByCreate private val bottomBarUiInteractor: BottomBarUiInteractor,
    @ByCreate private val filterBarUiInteractor: FilterBarUiInteractor,
    @ByCreate private val notificationsUiInteractor: NotificationsUiInteractor,
) : NavigationViewModel() {
    /**
     * Путь до лог файла, null если открыт live источник.
     */
    private val logPath: Path? = (source as? LogViewerSource.File)?.path

    private val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))
    private val selectedSearchIndex = MutableStateFlow(0)
    private val showSelectMappingDialog = MutableStateFlow(false)
    private val firstVisibleIndex = MutableStateFlow(0)
    private val showTagStat = MutableStateFlow(false)
    private val followTail = MutableStateFlow(source is LogViewerSource.DeviceLogcat)

    private val logsInteractor: LogsInteractor = LogsInteractorImpl(
        scope = viewModelScope,
        dispatchers = dispatchers,
        source = when (source) {
            is LogViewerSource.File -> LogsSource.File(source.path)

            is LogViewerSource.DeviceLogcat -> LogsSource.LiveFlow(
                LiveProcessNameEnricher(adbClient, source.deviceName).enrich(
                    logParserProvider.getBinaryFlowLogParser().parseLog(
                        adbClient.observeBinaryLogcat(source.deviceName)
                            .map { it.unwrap() },
                    ),
                ),
            )
        },
        logParserProvider = logParserProvider,
        notificationsUiInteractor = notificationsUiInteractor,
        proguardInteractor = mappingPath?.let { ProguardInteractorImpl(it) },
    )

    init {
        if (logPath != null) {
            launch {
                logRecentInteractor.addOrUpdateRecent(logPath)
                if (mappingPath != null) {
                    logRecentInteractor.updateMappingPath(logPath, mappingPath)
                } else {
                    val mapping = logRecentInteractor.getMappingPath(logPath)
                    if (mapping != null) {
                        logsInteractor.attachMapping(mapping)
                    }
                }

                val searchState = logRecentInteractor.getLogViewerState(logPath)
                if (searchState != null) {
                    onSearchChange(searchState.searchRequest)
                    filterBarUiInteractor.setFilter(searchState.filterRequest)
                }
            }

            // TODO подумать и сделать нормально
            relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
                try {
                    delay(Long.MAX_VALUE)
                } catch (_: CancellationException) {
                    LogViewerLogger.d { "Saving current search && filter data into recents" }
                    withContext(NonCancellable) {
                        logRecentInteractor.updateLogViewerState(
                            path = logPath,
                            searchRequest = search.value.search,
                            filterRequest = filterBarUiInteractor.filterState.first().requestHighlight.raw,
                            selectedSearchIndex = selectedSearchIndex.value,
                            scrollPosition = firstVisibleIndex.value,
                        )
                    }
                }
            }
        }

        logsInteractor.observeLogs()
            .map { it.map { it.raw.substring(it.tag) }.toSet() }
            .distinctUntilChanged()
            .linkTo(currentTags)
        logsInteractor.observeLogs()
            .map { logs -> logs.mapNotNull { log -> log.processName?.let { log.raw.substring(it) } }.toSet() }
            .distinctUntilChanged()
            .linkTo(currentPackages)
        logsInteractor.observeRuns()
            .map { it ?: emptyList() }
            .linkTo(currentRuns)
    }

    private var isOpenedOnce = false

    val tabState = when (source) {
        is LogViewerSource.File -> logRecentInteractor.observeCustomName(source.path)
            .map { TabSupport.TabState(name = it ?: source.path.name, allowDetach = true) }
            .stateIn(TabSupport.TabState(allowDetach = true))

        is LogViewerSource.DeviceLogcat -> MutableStateFlow(
            TabSupport.TabState(name = "logcat: ${source.deviceName}", allowDetach = true),
        )
    }

    val state: StateFlow<LogViewerViewState> = combine(
        logsInteractor.observeLogIndex(
            filter = filterBarUiInteractor.filterState.mapNotNull { it.searchRequest.getOrNull() },
            search = search,
        )
            .onEach {
                // TODO отлично onEach стал еще больше. Теперь тут двойной костыль.
                @Suppress("ComplexCondition")
                if (!isOpenedOnce &&
                    !it.isSearchingNow &&
                    !it.isFilteringNow &&
                    logsInteractor.observeLoadingStatus().value is LogsInteractor.LoadingStatus.Loaded &&
                    it.lastSuccessIndex.logs.isNotEmpty()
                ) {
                    isOpenedOnce = true
                    val state = logPath?.let { logRecentInteractor.getLogViewerState(it) }
                    if (state != null) {
                        if (state.selectedSearchIndex >= 0) selectedSearchIndex.value = state.selectedSearchIndex
                        if (state.scrollPosition >= 0) scrollToIndex(state.scrollPosition)
                    }
                } else {
                    // TODO убрать эту жесть, ну какой onEach?
                    if (it.lastSuccessIndex.searchIndex.index.isNotEmpty()) {
                        var selectedIndex = 0

                        // Ищем первый подходящий индекс в видимой части экрана или за ее пределами снизу.
                        // Если такого нет, то берем первый индекс выше видимой части экрана.
                        for ((index, recordIndex) in it.lastSuccessIndex.searchIndex.index.withIndex()) {
                            selectedIndex = index
                            if (recordIndex >= firstVisibleIndex.value) break
                        }

                        selectedSearchIndex.value = selectedIndex
                        scrollToRecordIndex(it.lastSuccessIndex.searchIndex.index[selectedSearchIndex.value])
                    } else {
                        selectedSearchIndex.value = 0
                    }
                }
            },
        search,
        selectedSearchIndex,
        logsInteractor.observeMappingStatus(),
        showSelectMappingDialog,
        logViewerSettingsRepository.isStripDateEnabled,
        logViewerSettingsRepository.logFontSize,
        showTagStat,
        followTail,
    ) {
            logIndexProgress,
            search,
            selectedSearchIndex,
            mappingStatus,
            showSelectMappingDialog,
            stripDate,
            logFontSize,
            showTagStat,
            followTail,
        ->

        val runIdOrders = logIndexProgress.lastSuccessIndex.runIdOrders
        val logsWithRunNumber = if (runIdOrders == null) {
            listOf(
                LogsViewState.SectionInfo(
                    logs = logIndexProgress.lastSuccessIndex.logs,
                    meta = null,
                ),
            )
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
                LogsViewState.SectionInfo(
                    logs = items,
                    meta = info.meta,
                )
            }
        }

        val currentSelectedItemOrder = logIndexProgress.lastSuccessIndex.logs.getOrNull(
            logIndexProgress.lastSuccessIndex.searchIndex.index.getOrNull(selectedSearchIndex) ?: -1,
        )?.order ?: LogOrder(-1)

        LogViewerViewState(
            searchIndex = logIndexProgress.lastSuccessIndex.searchIndex.index,
            logsViewState = LogsViewState(
                logs = logsWithRunNumber,
                rawLogs = logIndexProgress.lastSuccessIndex.logs,
                runIdOrders = logIndexProgress.lastSuccessIndex.runIdOrders,
                currentSelectedItemOrder = currentSelectedItemOrder,
                showRunNumbers = runIdOrders != null,
                maxLogNumberDigits = (logIndexProgress.lastSuccessIndex.totalLogRecords + 1).toString().length,
                stripDate = stripDate,
                logFontSize = logFontSize,
                followTail = followTail,
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
            isMappingSupported = source is LogViewerSource.File,
            isFollowTailSupported = source is LogViewerSource.DeviceLogcat,
            isStripDate = stripDate,
            showSelectMappingDialog = showSelectMappingDialog,
            logRecordsAfterApplyFilter = logIndexProgress.lastSuccessIndex.logs.size,
            showTagStat = showTagStat,
        )
    }
        // TODO ломает строку поиска.
        // .flowOn(dispatchers.Default)
        .stateIn(LogViewerViewState.STUB)

    val logsEvents = Channel<LogsEvents>()

    init {
        launch {
            logsInteractor.observeLoadingStatus().collectLatest {
                when (it) {
                    is LogsInteractor.LoadingStatus.Loaded -> Unit

                    is LogsInteractor.LoadingStatus.LoadingLogs -> {
                        bottomBarUiInteractor.showProgressBar(
                            Res.string.log_viewer_bottom_loading.asResourceString(),
                        )
                    }

                    is LogsInteractor.LoadingStatus.DeobfuscateLogs -> {
                        bottomBarUiInteractor.showProgressBar(
                            Res.string.log_viewer_bottom_deobfuscate.asResourceString(),
                        )
                    }
                }
            }
        }
        launch {
            state
                .resubscribeOnUiLifecycle(Lifecycle.State.RESUMED)
                .collect { state ->
                    bottomBarUiInteractor.setBottomBarText(
                        Res.string.log_viewer_bottom_total_records.asResourceString(
                            state.logRecordsAfterApplyFilter,
                        ),
                    )
                }
        }
    }

    /**
     * Скролит к записи логов по ее индексу автоматически добавляет офсет заголовка
     */
    private fun scrollToRecordIndex(index: Int) = launch {
        followTail.value = false
        val logs = state.value.logsViewState
        val additionalIndex = logs.runIdOrders?.let { runs ->
            val order = logs.rawLogs[index].order
            runs.indexOfFirst { order in it.orderRange }
        } ?: -1
        val finalIndex = if (additionalIndex == -1) {
            index
        } else {
            index + additionalIndex + 1
        }
        scrollToIndex(finalIndex)
    }

    /**
     * Скролит к выбранному индексу, учитывайте что заголовки (RunNumber) тоже нужно учитывать.
     */
    private fun scrollToIndex(index: Int) = launch {
        LogViewerLogger.d { "Scroll to index $index" }
        logsEvents.send(LogsEvents.ScrollToIndex(index))
    }

    fun onClickMappingButton() = launch {
        if (state.value.isMappingApplied) {
            logsInteractor.detachMapping()
            logPath?.let { logRecentInteractor.updateMappingPath(it, null) }
        } else {
            showSelectMappingDialog.value = true
        }
    }

    fun onSelectMappingDialogResult(paths: List<Path>) = launch {
        showSelectMappingDialog.value = false
        paths.firstOrNull()?.let { result ->
            logsInteractor.attachMapping(result)
            logPath?.let { logRecentInteractor.updateMappingPath(it, result) }
        }
    }

    fun onClickStripDate() = launch {
        logViewerSettingsRepository.setIsStripDateEnabled(!state.value.isStripDate)
    }

    fun onClickFontUp() = launch {
        logViewerSettingsRepository.setLogFontSize(state.value.logsViewState.logFontSize + 1)
    }

    fun onClickFontDown() = launch {
        logViewerSettingsRepository.setLogFontSize((state.value.logsViewState.logFontSize - 1).coerceAtLeast(1))
    }

    fun onClickShowTagStat() {
        showTagStat.update { !it }
    }

    fun onClickFollowTail() {
        followTail.update { !it }
    }

    fun onUserScroll() {
        followTail.value = false
    }

    fun onDragAndDropLogsFiles(paths: List<Path>) {
        paths.forEach { open(LogViewerScreenParams(logPath = it)) }
    }

    fun onDragAndDropMappingFiles(paths: List<Path>) = launch {
        if (paths.size != 1) {
            notificationsUiInteractor.showNotification(
                Notification(
                    Res.string.log_viewer_mapping_expected_one.asResourceString(paths.size),
                    Notification.Servility.Error,
                ),
            )
            return@launch
        }
        logsInteractor.attachMapping(paths.single())
    }

    fun onClickPrevIndex() {
        if (state.value.searchIndex.isNotEmpty()) {
            if (selectedSearchIndex.value == 0) {
                selectedSearchIndex.value = state.value.searchIndex.size - 1
            } else {
                selectedSearchIndex.value -= 1
            }
            scrollToRecordIndex(state.value.searchIndex[selectedSearchIndex.value])
        }
    }

    fun onClickNextIndex() {
        if (state.value.searchIndex.isNotEmpty()) {
            if (selectedSearchIndex.value == state.value.searchIndex.size - 1) {
                selectedSearchIndex.value = 0
            } else {
                selectedSearchIndex.value += 1
            }
            scrollToRecordIndex(state.value.searchIndex[selectedSearchIndex.value])
        }
    }

    fun onFirstVisibleIndexUpdate(index: Int) {
        firstVisibleIndex.value = index
    }

    fun onAddTimeFilter(order: LogOrder, isAfter: Boolean) {
        val log = state.value.logsViewState.rawLogs.find { it.order == order } ?: return
        val time = log.raw.substring(log.time)
        val prefix = if (isAfter) "timeAfter" else "timeBefore"
        filterBarUiInteractor.addToFilter("$prefix=\"$time\"")
    }

    fun onSearchChange(newValue: String) = search.update { it.copy(search = newValue) }
    fun onClickSearchMatchCase(newValue: Boolean) = search.update { it.copy(matchCase = newValue) }
    fun onClickSearchUseRegex(newValue: Boolean) = search.update { it.copy(useRegex = newValue) }
    fun onClickScrollToBottom() {
        logsEvents.trySend(LogsEvents.ScrollToIndex(Int.MAX_VALUE))
    }
}
