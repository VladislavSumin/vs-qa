package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logParser.anime.domain.AnimeLogParserProvider
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.runId.RawRunIdInfo
import ru.vladislavsumin.feature.logParser.domain.substring
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractor
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.Notification
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("MaximumLineLength", "MaxLineLength")
class LogsInteractorImplTest {

    private val logPath = resolveTestLogPath()
    private val logParserProvider = AnimeLogParserProvider()

    private fun resolveTestLogPath(): Path {
        var dir: Path = Path("").toAbsolutePath()
        while (true) {
            val candidate = dir.resolve("test-data/sample-anime.log")
            if (candidate.toFile().exists()) return candidate
            dir = dir.parent ?: error("Cannot find test-data/sample-anime.log from working directory")
        }
    }

    init {
        LoggerManager.initTest()
    }

    // --- Test infrastructure ---

    @Suppress("LeakingThis")
    private class TestDispatchers(override val Default: CoroutineDispatcher, override val IO: CoroutineDispatcher) :
        VsDispatchers {
        override val Main: MainCoroutineDispatcher get() = error("Main not used")
        override val Unconfined: CoroutineDispatcher get() = IO
    }

    private class FakeNotificationsUiInteractor : NotificationsUiInteractor {
        val notifications = mutableListOf<Notification>()
        override suspend fun showNotification(notification: Notification) {
            notifications.add(notification)
        }
    }

    private class FakeProguardInteractor(private val warmupResult: Result<Unit> = Result.success(Unit)) :
        ProguardInteractor {
        var warmupCalled = false

        override fun warmup(): Result<Unit> {
            warmupCalled = true
            return warmupResult
        }

        override fun deobfuscateClass(obfuscatedClassName: String): String? = null
        override fun deobfuscateStack(data: String): String = data
    }

    private fun createInteractor(
        testDispatcher: CoroutineDispatcher,
        proguardInteractor: ProguardInteractor? = null,
        source: LogsSource = LogsSource.File(logPath),
    ): LogsInteractorImpl = LogsInteractorImpl(
        scope = CoroutineScope(testDispatcher + Job()),
        dispatchers = TestDispatchers(Default = testDispatcher, IO = testDispatcher),
        source = source,
        logParserProvider = logParserProvider,
        notificationsUiInteractor = FakeNotificationsUiInteractor(),
        proguardInteractor = proguardInteractor,
    )

    // --- Group 1: Lifecycle & loading ---

    @Test
    fun `loading without mapping LoadingLogs to Loaded`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        assertEquals(160, interactor.observeLogs().first().size)
        val status = interactor.observeLoadingStatus().value
        assertTrue(status is LogsInteractor.LoadingStatus.Loaded)
        assertFalse((status as LogsInteractor.LoadingStatus.Loaded).isDeobfuscated)
    }

    @Test
    fun `loading with mapping LoadingLogs to DeobfuscateLogs to Loaded`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeProguard = FakeProguardInteractor()
        val interactor = createInteractor(testDispatcher = dispatcher, proguardInteractor = fakeProguard)
        advanceUntilIdle()

        assertEquals(160, interactor.observeLogs().first().size)
        val status = interactor.observeLoadingStatus().value
        assertTrue(status is LogsInteractor.LoadingStatus.Loaded)
        assertTrue((status as LogsInteractor.LoadingStatus.Loaded).isDeobfuscated)
        assertTrue(fakeProguard.warmupCalled)
    }

    @Test
    fun `mapping starts as NotAttached when no proguard`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        assertEquals(
            LogsInteractor.MappingStatus.NotAttached,
            interactor.observeMappingStatus().value,
        )
    }

    @Test
    fun `mapping is Attached when proguard provided`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(
            testDispatcher = dispatcher,
            proguardInteractor = FakeProguardInteractor(),
        )
        advanceUntilIdle()

        assertEquals(
            LogsInteractor.MappingStatus.Attached,
            interactor.observeMappingStatus().value,
        )
    }

    @Test
    fun `detachMapping sets NotAttached`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeProguard = FakeProguardInteractor()
        val interactor = createInteractor(testDispatcher = dispatcher, proguardInteractor = fakeProguard)
        advanceUntilIdle()
        assertTrue(interactor.observeLoadingStatus().value is LogsInteractor.LoadingStatus.Loaded)

        interactor.detachMapping()
        advanceUntilIdle()

        assertEquals(
            LogsInteractor.MappingStatus.NotAttached,
            interactor.observeMappingStatus().value,
        )
    }

    @Test
    fun `observeRuns returns null for sample log`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val runs = interactor.observeRuns().first()
        assertNull(runs)
    }

    @Test
    fun `live source accumulates records incrementally`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        fun record(index: Int): RawLogRecord {
            val raw = "2026-07-01T09:00:0${index}Z I Tag message $index"
            return RawLogRecord(
                raw = raw,
                time = LogRange(0, raw.indexOf(' ') - 1),
                timeDate = LogRange(0, 9),
                timeInstant = Instant.parse("2026-07-01T09:00:0$index.000Z"),
                level = LogRange(raw.indexOf('I'), raw.indexOf('I')),
                logLevel = LogLevel.INFO,
                processId = null,
                thread = LogRange(0, 0),
                tag = LogRange(0, 0),
                message = LogRange(0, raw.length - 1),
                lines = 1,
            )
        }

        val records = MutableSharedFlow<RawLogRecord>()
        val interactor = createInteractor(
            testDispatcher = dispatcher,
            source = LogsSource.LiveFlow(records),
        )
        advanceUntilIdle()

        assertTrue(interactor.observeLoadingStatus().value is LogsInteractor.LoadingStatus.Loaded)
        assertEquals(0, interactor.observeLogs().first().size)

        records.emit(record(0))
        advanceUntilIdle()
        assertEquals(1, interactor.observeLogs().first().size)

        records.emit(record(1))
        records.emit(record(2))
        advanceUntilIdle()

        val logs = interactor.observeLogs().first()
        assertEquals(3, logs.size)
        assertEquals(listOf(LogOrder(0), LogOrder(1), LogOrder(2)), logs.map { it.order })
        assertTrue(logs[2].raw.endsWith("message 2"))
        assertNull(interactor.observeRuns().first())
    }

    // --- Group 2: observeLogIndex ---

    @Test
    fun `empty filter empty search all records NoSearch`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(FilterRequest(FilterRequest.FilterOperation.NoOp))
        val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(results.isNotEmpty(), "Expected at least one emission")
        val last = results.last()
        assertEquals(160, last.lastSuccessIndex.totalLogRecords)
        assertEquals(160, last.lastSuccessIndex.logs.size)
        assertTrue(last.lastSuccessIndex.searchIndex is LogIndex.SearchIndex.NoSearch)
        assertFalse(last.isFilteringNow)
        assertFalse(last.isSearchingNow)
    }

    @Test
    fun `filter by level ERROR`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(
            FilterRequest(FilterRequest.FilterOperation.MinLogLevel(LogLevel.ERROR)),
        )
        val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertEquals(12, last.lastSuccessIndex.logs.size)
        assertTrue(
            last.lastSuccessIndex.logs.all {
                it.logLevel.rawLevel >= LogLevel.ERROR.rawLevel
            },
        )
    }

    @Test
    fun `filter by tag`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val tagFilter = FilterRequest.FilterOperation.Tag(
            FilterRequest.Operation.Contains("BluetoothManager"),
        )
        val filter = MutableStateFlow(FilterRequest(tagFilter))
        val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertEquals(13, last.lastSuccessIndex.logs.size)
        assertTrue(
            last.lastSuccessIndex.logs.all {
                it.raw.substring(it.tag).contains("BluetoothManager", ignoreCase = true)
            },
        )
    }

    @Test
    fun `filter with AND logic`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val andFilter = FilterRequest.FilterOperation.And(
            listOf(
                FilterRequest.FilterOperation.Tag(FilterRequest.Operation.Contains("PaymentGateway")),
                FilterRequest.FilterOperation.MinLogLevel(LogLevel.ERROR),
            ),
        )
        val filter = MutableStateFlow(FilterRequest(andFilter))
        val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertEquals(4, last.lastSuccessIndex.logs.size)
        assertTrue(
            last.lastSuccessIndex.logs.all {
                it.raw.substring(it.tag) == "PaymentGateway" &&
                    it.logLevel == LogLevel.ERROR
            },
        )
    }

    @Test
    fun `search by text returns SearchIndex`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(FilterRequest(FilterRequest.FilterOperation.NoOp))
        val search = MutableStateFlow(SearchRequest(search = "timeout", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertFalse(last.isFilteringNow, "Expected filtering to be done")
        assertFalse(last.isSearchingNow, "Expected searching to be done")
        val searchIndex = last.lastSuccessIndex.searchIndex
        assertTrue(
            searchIndex is LogIndex.SearchIndex.Search,
            "Expected Search, got ${searchIndex::class.simpleName}",
        )
        assertTrue((searchIndex as LogIndex.SearchIndex.Search).index.isNotEmpty())
    }

    @Test
    fun `search with empty result returns EmptySearch`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(FilterRequest(FilterRequest.FilterOperation.NoOp))
        val search = MutableStateFlow(
            SearchRequest(search = "zzzNonexistentStringzzz", matchCase = false, useRegex = false),
        )

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertTrue(last.lastSuccessIndex.searchIndex is LogIndex.SearchIndex.EmptySearch)
    }

    @Test
    fun `regex search with invalid regex returns BadRegex`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(FilterRequest(FilterRequest.FilterOperation.NoOp))
        val search = MutableStateFlow(
            SearchRequest(search = "[invalid(", matchCase = false, useRegex = true),
        )

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertTrue(last.lastSuccessIndex.searchIndex is LogIndex.SearchIndex.BadRegex)
    }

    @Test
    fun `filter change resets search progress`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(FilterRequest(FilterRequest.FilterOperation.NoOp))
        val search = MutableStateFlow(SearchRequest(search = "timeout", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(results.isNotEmpty(), "Expected at least one emission, got none")
        val last = results.last()
        assertFalse(last.isFilteringNow)
        assertFalse(last.isSearchingNow)
        assertTrue(
            last.lastSuccessIndex.searchIndex is LogIndex.SearchIndex.Search,
            "Last emission should be Search: ${last.lastSuccessIndex.searchIndex::class.simpleName}",
        )
        assertTrue(last.lastSuccessIndex.logs.size == 160)
    }

    @Test
    fun `search is case-insensitive by default`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val filter = MutableStateFlow(FilterRequest(FilterRequest.FilterOperation.NoOp))
        val search = MutableStateFlow(
            SearchRequest(search = "BLUETOOTHMANAGER", matchCase = false, useRegex = false),
        )

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        val searchIndex = last.lastSuccessIndex.searchIndex
        assertTrue(searchIndex is LogIndex.SearchIndex.Search)
        assertTrue((searchIndex as LogIndex.SearchIndex.Search).index.isNotEmpty())
    }

    @Test
    fun `filter OR logic`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val interactor = createInteractor(testDispatcher = dispatcher)
        advanceUntilIdle()

        val orFilter = FilterRequest.FilterOperation.Or(
            listOf(
                FilterRequest.FilterOperation.Tag(FilterRequest.Operation.Contains("CrashHandler")),
                FilterRequest.FilterOperation.Tag(FilterRequest.Operation.Contains("PaymentGateway")),
            ),
        )
        val filter = MutableStateFlow(FilterRequest(orFilter))
        val search = MutableStateFlow(SearchRequest(search = "", matchCase = false, useRegex = false))

        val results = mutableListOf<LogIndexProgress>()
        val job = launch { interactor.observeLogIndex(filter, search).collect { results.add(it) } }
        advanceUntilIdle()
        job.cancel()

        val last = results.last()
        assertTrue(last.lastSuccessIndex.logs.isNotEmpty())
        assertTrue(
            last.lastSuccessIndex.logs.all {
                val tag = it.raw.substring(it.tag)
                tag == "CrashHandler" || tag == "PaymentGateway"
            },
        )
    }

    // --- Group 3: toRunIdInfo ---

    @Test
    fun `toRunIdInfo two runs correct orderRanges and duration`() {
        val now = Instant.parse("2026-07-01T09:00:00Z")

        fun record(second: Int): RawLogRecord {
            val time = now.plusSeconds(second.toLong())
            val raw = "$time I Tag message"
            return RawLogRecord(
                raw = raw,
                time = LogRange(0, raw.indexOf(' ')),
                timeDate = LogRange(0, 9),
                timeInstant = time,
                level = LogRange(raw.indexOf('I'), raw.indexOf('I')),
                logLevel = LogLevel.INFO,
                processId = null,
                thread = LogRange(0, 0),
                tag = LogRange(0, 0),
                message = LogRange(0, raw.length - 1),
                lines = 1,
            )
        }

        val logs = listOf(
            record(0),
            record(1),
            record(2),
            record(3),
            record(4),
            record(20),
            record(21),
            record(22),
        )
        val runIdInfos = listOf(
            RawRunIdInfo(0, "version" to "1.0"),
            RawRunIdInfo(5, "version" to "2.0"),
        )

        val result = runIdInfos.toRunIdInfo(logs)

        assertEquals(2, result.size)
        assertEquals(LogOrder(0), result[0].orderRange.start)
        assertEquals(LogOrder(4), result[0].orderRange.endInclusive)
        assertEquals("4s", result[0].meta["duration"])
        assertEquals(LogOrder(5), result[1].orderRange.start)
        assertEquals(LogOrder(Int.MAX_VALUE), result[1].orderRange.endInclusive)
        assertEquals("2s", result[1].meta["duration"])
    }

    @Test
    fun `toRunIdInfo single run covers all records`() {
        val now = Instant.parse("2026-07-01T09:00:00Z")

        fun record(second: Int): RawLogRecord {
            val time = now.plusSeconds(second.toLong())
            val raw = "$time I Tag message"
            return RawLogRecord(
                raw = raw,
                time = LogRange(0, raw.indexOf(' ')),
                timeDate = LogRange(0, 9),
                timeInstant = time,
                level = LogRange(raw.indexOf('I'), raw.indexOf('I')),
                logLevel = LogLevel.INFO,
                processId = null,
                thread = LogRange(0, 0),
                tag = LogRange(0, 0),
                message = LogRange(0, raw.length - 1),
                lines = 1,
            )
        }

        val logs = listOf(record(0), record(1), record(2))
        val runIdInfos = listOf(RawRunIdInfo(0, "version" to "1.0"))

        val result = runIdInfos.toRunIdInfo(logs)

        assertEquals(1, result.size)
        assertEquals(LogOrder(0), result[0].orderRange.start)
        assertEquals(LogOrder(Int.MAX_VALUE), result[0].orderRange.endInclusive)
        assertEquals("2s", result[0].meta["duration"])
    }

    @Test
    fun `toRunIdInfo empty run ids returns empty list`() {
        val result = emptyList<RawRunIdInfo>().toRunIdInfo(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toRunIdInfo preserves existing meta keys when adding duration`() {
        val now = Instant.parse("2026-07-01T09:00:00Z")
        val time = now.toString()
        val raw = "$time I Tag message"
        val log = RawLogRecord(
            raw = raw,
            time = LogRange(0, raw.indexOf(' ')),
            timeDate = LogRange(0, 9),
            timeInstant = now,
            level = LogRange(raw.indexOf('I'), raw.indexOf('I')),
            logLevel = LogLevel.INFO,
            processId = null,
            thread = LogRange(0, 0),
            tag = LogRange(0, 0),
            message = LogRange(0, raw.length - 1),
            lines = 1,
        )

        val runIdInfos = listOf(
            RawRunIdInfo(0, "version" to "4.2.1", "pid" to "12345"),
        )

        val result = runIdInfos.toRunIdInfo(listOf(log))

        assertEquals(1, result.size)
        assertNotNull(result[0].meta["version"])
        assertNotNull(result[0].meta["pid"])
        assertNotNull(result[0].meta["duration"])
    }
}
