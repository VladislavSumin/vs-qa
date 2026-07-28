package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.coroutines.utils.LinkedFlow
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.test.createTestNavigationViewModel
import ru.vladislavsumin.core.ui.resources.ResourceString
import ru.vladislavsumin.feature.logParser.domain.BinaryFlowLogParser
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractor
import ru.vladislavsumin.feature.logViewer.domain.logs.FilterRequest
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndex
import ru.vladislavsumin.feature.logViewer.domain.logs.LogIndexProgress
import ru.vladislavsumin.feature.logViewer.domain.logs.LogOrder
import ru.vladislavsumin.feature.logViewer.domain.logs.LogOrderRange
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractor
import ru.vladislavsumin.feature.logViewer.domain.logs.LogsInteractorFactory
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.domain.logs.SearchRequest
import ru.vladislavsumin.feature.logViewer.repository.LogViewerSettingsRepository
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarUiInteractor
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterRequestParser
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsEvents
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.Notification
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import java.nio.file.Path
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.io.path.Path as KPath

@Suppress("MaximumLineLength", "MaxLineLength", "LargeClass")
@OptIn(ExperimentalCoroutinesApi::class, InternalNavigationApi::class)
class LogViewerViewModelTest {

    init {
        LoggerManager.initTest()
    }

    // --- Test helpers ---

    private fun createLogRecord(
        order: Int,
        tag: String = "MyTag",
        message: String = "Log message $order",
        timeInstant: Instant = Instant.parse("2026-07-01T09:00:00Z"),
    ): LogRecord {
        val raw = "$timeInstant I $tag: $message"
        return LogRecord(
            order = LogOrder(order),
            raw = raw,
            time = LogRange(0, raw.indexOf(' ') - 1),
            timeDate = LogRange(0, 9),
            timeInstant = timeInstant,
            level = LogRange(raw.indexOf(" I ") + 1, raw.indexOf(" I ") + 1),
            processId = null,
            processName = null,
            thread = LogRange(0, 0),
            tag = LogRange(raw.indexOf(tag), raw.indexOf(tag) + tag.length - 1),
            message = LogRange(raw.indexOf(": ") + 2, raw.length - 1),
            searchHighlights = null,
            logLevel = LogLevel.INFO,
        )
    }

    // --- Fake implementations ---

    private class FakeLogsInteractor : LogsInteractor {
        val loadingStatusState = MutableStateFlow<LogsInteractor.LoadingStatus>(
            LogsInteractor.LoadingStatus.Loaded(false),
        )
        val mappingStatusState = MutableStateFlow<LogsInteractor.MappingStatus>(
            LogsInteractor.MappingStatus.NotAttached,
        )
        val logsState = MutableStateFlow<List<LogRecord>>(emptyList())
        val runsState = MutableStateFlow<List<RunIdInfo>?>(null)
        val logIndexState = MutableStateFlow(
            LogIndexProgress(
                isFilteringNow = false,
                isSearchingNow = false,
                lastSuccessIndex = LogIndex(
                    logs = emptyList(),
                    runIdOrders = null,
                    searchIndex = LogIndex.SearchIndex.NoSearch,
                    totalLogRecords = 0,
                ),
            ),
        )

        val attachMappingCalls = mutableListOf<Path>()
        var detachMappingCalled = false

        override fun observeLoadingStatus(): StateFlow<LogsInteractor.LoadingStatus> = loadingStatusState
        override fun observeMappingStatus(): StateFlow<LogsInteractor.MappingStatus> = mappingStatusState
        override fun observeLogs(): Flow<List<LogRecord>> = logsState
        override fun observeRuns(): Flow<List<RunIdInfo>?> = runsState

        override fun observeLogIndex(filter: Flow<FilterRequest>, search: Flow<SearchRequest>): Flow<LogIndexProgress> =
            logIndexState

        override suspend fun detachMapping() {
            detachMappingCalled = true
            mappingStatusState.value = LogsInteractor.MappingStatus.NotAttached
        }

        override suspend fun attachMapping(path: Path) {
            attachMappingCalls.add(path)
            mappingStatusState.value = LogsInteractor.MappingStatus.Attached
        }
    }

    private class FakeLogsInteractorFactory(private val interactor: FakeLogsInteractor) : LogsInteractorFactory {
        override fun create(
            scope: kotlinx.coroutines.CoroutineScope,
            source: ru.vladislavsumin.feature.logViewer.domain.logs.LogsSource,
            notificationsUiInteractor: NotificationsUiInteractor,
            proguardInteractor: ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractor?,
        ): LogsInteractor = interactor
    }

    private class FakeLogViewerSettingsRepository : LogViewerSettingsRepository {
        val stripDateEnabledState = MutableStateFlow(false)
        val logFontSizeState = MutableStateFlow(12)

        val setIsStripDateEnabledCalls = mutableListOf<Boolean>()
        val setLogFontSizeCalls = mutableListOf<Int>()

        override val isStripDateEnabled: Flow<Boolean> = stripDateEnabledState
        override val logFontSize: Flow<Int> = logFontSizeState

        override suspend fun setIsStripDateEnabled(isEnabled: Boolean) {
            setIsStripDateEnabledCalls.add(isEnabled)
            stripDateEnabledState.value = isEnabled
        }

        override suspend fun setLogFontSize(size: Int) {
            setLogFontSizeCalls.add(size)
            logFontSizeState.value = size
        }
    }

    private class FakeLogRecentInteractor : LogRecentInteractor {
        val addOrUpdateRecentCalls = mutableListOf<Path>()
        val updateMappingPathCalls = mutableListOf<Pair<Path, Path?>>()
        val updateLogViewerStateCalls = mutableListOf<LogRecentInteractor.LogViewerState>()

        var mappingPathToReturn: Path? = null
        var logViewerStateToReturn: LogRecentInteractor.LogViewerState? = null
        val customNameState = MutableStateFlow<String?>(null)

        override suspend fun addOrUpdateRecent(path: Path) {
            addOrUpdateRecentCalls.add(path)
        }

        override suspend fun updateMappingPath(path: Path, mappingPath: Path?) {
            updateMappingPathCalls.add(path to mappingPath)
        }

        override suspend fun getMappingPath(path: Path): Path? = mappingPathToReturn

        override suspend fun updateLogViewerState(
            path: Path,
            searchRequest: String,
            filterRequest: String,
            selectedSearchIndex: Int,
            scrollPosition: Int,
            scrollPositionOffset: Int,
        ) {
            updateLogViewerStateCalls.add(
                LogRecentInteractor.LogViewerState(
                    searchRequest = searchRequest,
                    filterRequest = filterRequest,
                    selectedSearchIndex = selectedSearchIndex,
                    scrollPosition = scrollPosition,
                    scrollPositionOffset = scrollPositionOffset,
                ),
            )
        }

        override suspend fun getLogViewerState(path: Path): LogRecentInteractor.LogViewerState? = logViewerStateToReturn

        override fun observeCustomName(path: Path): Flow<String?> = customNameState
    }

    private class FakeBottomBarUiInteractor : BottomBarUiInteractor {
        var lastProgressBarText: ResourceString? = null
        var lastBottomBarText: ResourceString? = null

        override suspend fun showProgressBar(text: ResourceString): Nothing {
            lastProgressBarText = text
            error("showProgressBar returns Nothing — should not be reached in test")
        }

        override fun setBottomBarText(text: ResourceString) {
            lastBottomBarText = text
        }
    }

    private class FakeFilterBarUiInteractor : FilterBarUiInteractor {
        val filterStateSource = MutableStateFlow(
            FilterRequestParser.ParserResult(
                requestHighlight = FilterRequestParser.RequestHighlight.Success("", emptyList()),
                searchRequest = Result.success(FilterRequest(FilterRequest.FilterOperation.NoOp)),
                currentTokenPredictionInfo = null,
            ),
        )

        var setFilterCalled: String? = null
        var addToFilterCalled: String? = null

        override val filterState: Flow<FilterRequestParser.ParserResult> = filterStateSource

        override fun setFilter(data: String) {
            setFilterCalled = data
        }

        override fun addToFilter(part: String) {
            addToFilterCalled = part
        }
    }

    private class FakeNotificationsUiInteractor : NotificationsUiInteractor {
        val notifications = mutableListOf<Notification>()

        override suspend fun showNotification(notification: Notification) {
            notifications.add(notification)
        }
    }

    private class StubLogParserProvider : LogParserProvider {
        override val name: String = "stub"
        override fun getFileLogParser() = error("not used in tests")
        override fun getStringFlowLogParser() = error("not used in tests")
        override fun getBinaryFlowLogParser(): BinaryFlowLogParser = object : BinaryFlowLogParser {
            override fun parseLog(data: Flow<ByteArray>): Flow<RawLogRecord> = emptyFlow()
        }
    }

    private class StubAdbClient : AdbClient {
        override fun observeDevices(): Flow<AdbClient.AdbResult<List<AdbClient.DeviceInfo>>> = emptyFlow()

        override suspend fun executeShellCommand(deviceName: String, shellCommand: String) = error("not used in tests")

        override fun observeLogcat(
            deviceName: String,
            format: AdbClient.LogcatOutputFormat,
        ): Flow<AdbClient.AdbResult<String>> = emptyFlow()

        override fun observeBinaryLogcat(deviceName: String): Flow<AdbClient.AdbResult<ByteArray>> = emptyFlow()

        override suspend fun listProcesses(deviceName: String) = error("not used in tests")
        override suspend fun pullFile(deviceName: String, remotePath: String, localPath: String) =
            error("not used in tests")
    }

    // --- ViewModel factory ---

    private object TestDispatchers : VsDispatchers {
        override val Main: MainCoroutineDispatcher get() = Dispatchers.Main
        override val Default: CoroutineDispatcher get() = Dispatchers.Unconfined
        override val Unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
        override val IO: CoroutineDispatcher get() = Dispatchers.Unconfined
    }

    private data class TestDeps(
        val logsInteractor: FakeLogsInteractor = FakeLogsInteractor(),
        val settings: FakeLogViewerSettingsRepository = FakeLogViewerSettingsRepository(),
        val recents: FakeLogRecentInteractor = FakeLogRecentInteractor(),
        val bottomBar: FakeBottomBarUiInteractor = FakeBottomBarUiInteractor(),
        val filterBar: FakeFilterBarUiInteractor = FakeFilterBarUiInteractor(),
        val notifications: FakeNotificationsUiInteractor = FakeNotificationsUiInteractor(),
        val logParserProvider: LogParserProvider = StubLogParserProvider(),
        val adbClient: AdbClient = StubAdbClient(),
        val dispatchers: VsDispatchers = TestDispatchers,
    )

    private fun createViewModel(
        deps: TestDeps = TestDeps(),
        source: LogViewerSource = LogViewerSource.File(KPath("/tmp/test.log")),
        mappingPath: Path? = null,
    ): LogViewerViewModel = createTestNavigationViewModel {
        LogViewerViewModel(
            logParserProvider = deps.logParserProvider,
            logsInteractorFactory = FakeLogsInteractorFactory(deps.logsInteractor),
            logViewerSettingsRepository = deps.settings,
            logRecentInteractor = deps.recents,
            adbClient = deps.adbClient,
            source = source,
            mappingPath = mappingPath,
            currentTags = LinkedFlow(),
            currentPackages = LinkedFlow(),
            currentRuns = LinkedFlow(),
            bottomBarUiInteractor = deps.bottomBar,
            filterBarUiInteractor = deps.filterBar,
            notificationsUiInteractor = deps.notifications,
            dispatchers = deps.dispatchers,
        )
    }

    // ====================================================================
    // Group 1: Search
    // ====================================================================

    @Test
    fun `search change updates search request in state`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel()
        vm.onSearchChange("crash")
        assertEquals("crash", vm.state.value.searchState.searchRequest)
    }

    @Test
    fun `search match case toggles in state`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel()
        vm.onClickSearchMatchCase(true)
        assertTrue(vm.state.value.searchState.isMatchCase)
        vm.onClickSearchMatchCase(false)
        assertFalse(vm.state.value.searchState.isMatchCase)
    }

    @Test
    fun `search use regex toggles in state`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel()
        vm.onClickSearchUseRegex(true)
        assertTrue(vm.state.value.searchState.isRegex)
        vm.onClickSearchUseRegex(false)
        assertFalse(vm.state.value.searchState.isRegex)
    }

    @Test
    fun `prev index wraps to end when at first result`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = listOf(createLogRecord(0), createLogRecord(1), createLogRecord(2)),
                runIdOrders = null,
                searchIndex = LogIndex.SearchIndex.Search(listOf(0, 1, 2)),
                totalLogRecords = 3,
            ),
        )
        val vm = createViewModel(deps = deps)
        vm.onClickPrevIndex()
        assertEquals(2, vm.state.value.searchState.currentSearchResultIndex)
    }

    @Test
    fun `next index wraps to start when at last result`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = listOf(createLogRecord(0), createLogRecord(1), createLogRecord(2)),
                runIdOrders = null,
                searchIndex = LogIndex.SearchIndex.Search(listOf(0, 1, 2)),
                totalLogRecords = 3,
            ),
        )
        val vm = createViewModel(deps = deps)
        vm.onClickNextIndex()
        assertEquals(1, vm.state.value.searchState.currentSearchResultIndex)
    }

    @Test
    fun `bad regex detected in search state`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = listOf(createLogRecord(0)),
                runIdOrders = null,
                searchIndex = LogIndex.SearchIndex.BadRegex,
                totalLogRecords = 1,
            ),
        )
        val vm = createViewModel(deps = deps)
        assertTrue(vm.state.value.searchState.isBadRegex)
    }

    // ====================================================================
    // Group 2: Settings
    // ====================================================================

    @Test
    fun `strip date toggles and calls repository`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val vm = createViewModel(deps = deps)
        vm.onClickStripDate()
        assertEquals(listOf(true), deps.settings.setIsStripDateEnabledCalls)
        assertTrue(vm.state.value.isStripDate)
    }

    @Test
    fun `font up increases font size`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.settings.logFontSizeState.value = 12
        val vm = createViewModel(deps = deps)
        vm.onClickFontUp()
        assertEquals(listOf(13), deps.settings.setLogFontSizeCalls)
    }

    @Test
    fun `font down decreases font size`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.settings.logFontSizeState.value = 12
        val vm = createViewModel(deps = deps)
        vm.onClickFontDown()
        assertEquals(listOf(11), deps.settings.setLogFontSizeCalls)
    }

    @Test
    fun `font down clamped to minimum one`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.settings.logFontSizeState.value = 1
        val vm = createViewModel(deps = deps)
        vm.onClickFontDown()
        assertEquals(listOf(1), deps.settings.setLogFontSizeCalls)
    }

    // ====================================================================
    // Group 3: UI toggles
    // ====================================================================

    @Test
    fun `show tag stat toggles`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel()
        vm.onClickShowTagStat()
        assertTrue(vm.state.value.showTagStat)
        vm.onClickShowTagStat()
        assertFalse(vm.state.value.showTagStat)
    }

    @Test
    fun `follow tail toggles for file source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertFalse(vm.state.value.logsViewState.followTail)
        vm.onClickFollowTail()
        assertTrue(vm.state.value.logsViewState.followTail)
        vm.onClickFollowTail()
        assertFalse(vm.state.value.logsViewState.followTail)
    }

    @Test
    fun `follow tail is enabled by default for device logcat source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.DeviceLogcat("device1"))
        assertTrue(vm.state.value.logsViewState.followTail)
    }

    @Test
    fun `user scroll disables follow tail`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.DeviceLogcat("device1"))
        assertTrue(vm.state.value.logsViewState.followTail)
        vm.onUserScroll()
        assertFalse(vm.state.value.logsViewState.followTail)
    }

    @Test
    fun `scroll to bottom sends max int scroll event`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel()
        val received = mutableListOf<LogsEvents>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            for (event in vm.logsEvents) {
                received.add(event)
            }
        }
        vm.onClickScrollToBottom()
        assertTrue(received.isNotEmpty(), "Expected scroll event to be received")
        assertTrue(received.single() is LogsEvents.ScrollToIndex)
        assertEquals(Int.MAX_VALUE, (received.single() as LogsEvents.ScrollToIndex).index)
        job.cancel()
    }

    // ====================================================================
    // Group 4: Mapping
    // ====================================================================

    @Test
    fun `mapping button when not applied shows dialog`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel()
        assertFalse(vm.state.value.showSelectMappingDialog)
        vm.onClickMappingButton()
        assertTrue(vm.state.value.showSelectMappingDialog)
    }

    @Test
    fun `mapping button when applied detaches and clears recents`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.logsInteractor.mappingStatusState.value = LogsInteractor.MappingStatus.Attached
        val vm = createViewModel(deps = deps)
        vm.onClickMappingButton()
        assertTrue(deps.logsInteractor.detachMappingCalled)
        val expectedUpdates: List<Pair<Path, Path?>> = listOf(KPath("/tmp/test.log") to null)
        assertEquals(expectedUpdates, deps.recents.updateMappingPathCalls)
    }

    @Test
    fun `select mapping dialog result attaches mapping and updates recents`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val vm = createViewModel(deps = deps)
        vm.onSelectMappingDialogResult(listOf(KPath("/tmp/mapping.txt")))
        val expectedMappingCalls: List<Path> = listOf(KPath("/tmp/mapping.txt"))
        assertEquals(expectedMappingCalls, deps.logsInteractor.attachMappingCalls)
        val expectedRecentsUpdate: List<Pair<Path, Path?>> = listOf(
            KPath("/tmp/test.log") to KPath("/tmp/mapping.txt"),
        )
        assertEquals(expectedRecentsUpdate, deps.recents.updateMappingPathCalls)
        assertFalse(vm.state.value.showSelectMappingDialog)
    }

    @Test
    fun `select mapping dialog result with empty list does nothing`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val vm = createViewModel(deps = deps)
        vm.onSelectMappingDialogResult(emptyList())
        assertTrue(deps.logsInteractor.attachMappingCalls.isEmpty())
        assertFalse(vm.state.value.showSelectMappingDialog)
    }

    @Test
    fun `drag and drop one mapping file attaches mapping`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val vm = createViewModel(deps = deps)
        vm.onDragAndDropMappingFiles(listOf(KPath("/tmp/mapping.txt")))
        assertEquals(listOf(KPath("/tmp/mapping.txt")), deps.logsInteractor.attachMappingCalls)
        assertTrue(deps.notifications.notifications.isEmpty())
    }

    @Test
    fun `drag and drop two mapping files shows error notification`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val vm = createViewModel(deps = deps)
        vm.onDragAndDropMappingFiles(listOf(KPath("/tmp/a.txt"), KPath("/tmp/b.txt")))
        assertTrue(deps.logsInteractor.attachMappingCalls.isEmpty())
        assertEquals(1, deps.notifications.notifications.size)
        assertEquals(Notification.Servility.Error, deps.notifications.notifications.single().servility)
    }

    @Test
    fun `isMappingSupported true for file source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vmFile = createViewModel(source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertTrue(vmFile.state.value.isMappingSupported)
    }

    @Test
    fun `isMappingSupported false for logcat source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vmLogcat = createViewModel(source = LogViewerSource.DeviceLogcat("device1"))
        assertFalse(vmLogcat.state.value.isMappingSupported)
    }

    // ====================================================================
    // Group 5: Linked flows
    // ====================================================================

    @Test
    fun `observeLogs pushes tags to currentTags`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val currentTags = LinkedFlow<Set<String>>()
        val deps = TestDeps()
        createTestNavigationViewModel {
            LogViewerViewModel(
                logParserProvider = deps.logParserProvider,
                logsInteractorFactory = FakeLogsInteractorFactory(deps.logsInteractor),
                logViewerSettingsRepository = deps.settings,
                logRecentInteractor = deps.recents,
                adbClient = deps.adbClient,
                source = LogViewerSource.File(KPath("/tmp/test.log")),
                mappingPath = null,
                currentTags = currentTags,
                currentPackages = LinkedFlow(),
                currentRuns = LinkedFlow(),
                bottomBarUiInteractor = deps.bottomBar,
                filterBarUiInteractor = deps.filterBar,
                notificationsUiInteractor = deps.notifications,
                dispatchers = deps.dispatchers,
            )
        }
        deps.logsInteractor.logsState.value = listOf(createLogRecord(0, tag = "MyTag"))
        assertEquals(setOf("MyTag"), currentTags.first())
    }

    @Test
    fun `observeLogs pushes packages to currentPackages`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val currentPackages = LinkedFlow<Set<String>>()
        val deps = TestDeps()
        val raw = "2026-07-01T09:00:00Z I Tag: com.myapp Log message 0"
        val pkgStart = raw.indexOf("com.myapp")
        val record = createLogRecord(0).copy(
            processName = LogRange(pkgStart, pkgStart + "com.myapp".length - 1),
            raw = raw,
        )
        createTestNavigationViewModel {
            LogViewerViewModel(
                logParserProvider = deps.logParserProvider,
                logsInteractorFactory = FakeLogsInteractorFactory(deps.logsInteractor),
                logViewerSettingsRepository = deps.settings,
                logRecentInteractor = deps.recents,
                adbClient = deps.adbClient,
                source = LogViewerSource.File(KPath("/tmp/test.log")),
                mappingPath = null,
                currentTags = LinkedFlow(),
                currentPackages = currentPackages,
                currentRuns = LinkedFlow(),
                bottomBarUiInteractor = deps.bottomBar,
                filterBarUiInteractor = deps.filterBar,
                notificationsUiInteractor = deps.notifications,
                dispatchers = deps.dispatchers,
            )
        }
        deps.logsInteractor.logsState.value = listOf(record)
        assertEquals(setOf("com.myapp"), currentPackages.first())
    }

    @Test
    fun `observeRuns pushes runs to currentRuns`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val currentRuns = LinkedFlow<List<RunIdInfo>>()
        val deps = TestDeps()
        val runInfo = RunIdInfo(
            orderRange = LogOrderRange(LogOrder(0), LogOrder(5)),
            meta = mapOf("version" to "1.0"),
        )
        createTestNavigationViewModel {
            LogViewerViewModel(
                logParserProvider = deps.logParserProvider,
                logsInteractorFactory = FakeLogsInteractorFactory(deps.logsInteractor),
                logViewerSettingsRepository = deps.settings,
                logRecentInteractor = deps.recents,
                adbClient = deps.adbClient,
                source = LogViewerSource.File(KPath("/tmp/test.log")),
                mappingPath = null,
                currentTags = LinkedFlow(),
                currentPackages = LinkedFlow(),
                currentRuns = currentRuns,
                bottomBarUiInteractor = deps.bottomBar,
                filterBarUiInteractor = deps.filterBar,
                notificationsUiInteractor = deps.notifications,
                dispatchers = deps.dispatchers,
            )
        }
        deps.logsInteractor.runsState.value = listOf(runInfo)
        assertEquals(listOf(runInfo), currentRuns.first())
    }

    // ====================================================================
    // Group 6: Recents integration
    // ====================================================================

    @Test
    fun `init with logPath adds to recents`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val logPath = KPath("/tmp/test.log")
        createViewModel(deps = deps, source = LogViewerSource.File(logPath))
        assertEquals(listOf(logPath), deps.recents.addOrUpdateRecentCalls)
    }

    @Test
    fun `init does not add to recents for logcat source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        createViewModel(deps = deps, source = LogViewerSource.DeviceLogcat("device1"))
        assertTrue(deps.recents.addOrUpdateRecentCalls.isEmpty())
    }

    @Test
    fun `init with mappingPath saves mapping association to recents`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val logPath = KPath("/tmp/test.log")
        val mappingPath = KPath("/tmp/mapping.txt")
        createViewModel(deps = deps, source = LogViewerSource.File(logPath), mappingPath = mappingPath)
        val expectedMappingUpdates: List<Pair<Path, Path?>> = listOf(logPath to mappingPath)
        assertEquals(expectedMappingUpdates, deps.recents.updateMappingPathCalls)
    }

    @Test
    fun `init without mappingPath but recents has one attaches from recents`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.recents.mappingPathToReturn = KPath("/tmp/cached.txt")
        createViewModel(deps = deps, source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertEquals(listOf(KPath("/tmp/cached.txt")), deps.logsInteractor.attachMappingCalls)
    }

    @Test
    fun `init with saved search and filter state restores it`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.recents.logViewerStateToReturn = LogRecentInteractor.LogViewerState(
            searchRequest = "error",
            filterRequest = "tag=Test",
            selectedSearchIndex = 5,
            scrollPosition = 42,
            scrollPositionOffset = 0,
        )
        val vm = createViewModel(deps = deps, source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertEquals("error", vm.state.value.searchState.searchRequest)
        assertEquals("tag=Test", deps.filterBar.setFilterCalled)
    }

    // ====================================================================
    // Group 7: Tab state
    // ====================================================================

    @Test
    fun `file source tab name uses recents custom name when set`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.recents.customNameState.value = "Renamed Log"
        val vm = createViewModel(deps = deps, source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertEquals("Renamed Log", vm.tabState.value.name)
    }

    @Test
    fun `file source tab name falls back to filename when no custom name`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertEquals("test.log", vm.tabState.value.name)
    }

    @Test
    fun `logcat source tab name includes device name`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.DeviceLogcat("emulator-5554"))
        assertEquals("logcat: emulator-5554", vm.tabState.value.name)
    }

    // ====================================================================
    // Group 8: LogIndex -> state
    // ====================================================================

    @Test
    fun `isFollowTailSupported false for file source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.File(KPath("/tmp/test.log")))
        assertFalse(vm.state.value.isFollowTailSupported)
    }

    @Test
    fun `isFollowTailSupported true for logcat source`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val vm = createViewModel(source = LogViewerSource.DeviceLogcat("device1"))
        assertTrue(vm.state.value.isFollowTailSupported)
    }

    @Test
    fun `maxLogNumberDigits computed from totalLogRecords`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = listOf(createLogRecord(0)),
                runIdOrders = null,
                searchIndex = LogIndex.SearchIndex.NoSearch,
                totalLogRecords = 999,
            ),
        )
        val vm = createViewModel(deps = deps)
        assertEquals(4, vm.state.value.logsViewState.maxLogNumberDigits)
    }

    @Test
    fun `logRecordsAfterApplyFilter equals filtered logs size`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val logs = listOf(createLogRecord(0), createLogRecord(1), createLogRecord(2))
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = logs,
                runIdOrders = null,
                searchIndex = LogIndex.SearchIndex.NoSearch,
                totalLogRecords = 10,
            ),
        )
        val vm = createViewModel(deps = deps)
        assertEquals(3, vm.state.value.logRecordsAfterApplyFilter)
    }

    @Test
    fun `showRunNumbers is true when runIdOrders present`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        val runInfo = RunIdInfo(
            orderRange = LogOrderRange(LogOrder(0), LogOrder(2)),
            meta = mapOf("version" to "1.0"),
        )
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = listOf(createLogRecord(0), createLogRecord(1), createLogRecord(2)),
                runIdOrders = listOf(runInfo),
                searchIndex = LogIndex.SearchIndex.NoSearch,
                totalLogRecords = 3,
            ),
        )
        val vm = createViewModel(deps = deps)
        assertTrue(vm.state.value.logsViewState.runIdOrders != null)
    }

    @Test
    fun `showRunNumbers is false when runIdOrders is null`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val deps = TestDeps()
        deps.logsInteractor.logIndexState.value = LogIndexProgress(
            isFilteringNow = false,
            isSearchingNow = false,
            lastSuccessIndex = LogIndex(
                logs = listOf(createLogRecord(0)),
                runIdOrders = null,
                searchIndex = LogIndex.SearchIndex.NoSearch,
                totalLogRecords = 1,
            ),
        )
        val vm = createViewModel(deps = deps)
        assertFalse(vm.state.value.logsViewState.runIdOrders != null)
    }
}
