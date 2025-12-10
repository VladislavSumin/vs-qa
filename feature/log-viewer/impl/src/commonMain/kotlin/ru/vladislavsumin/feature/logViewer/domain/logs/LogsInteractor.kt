package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.coroutines.utils.mapState
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import ru.vladislavsumin.feature.logParser.domain.runId.RawRunIdInfo
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.delegates.filter.LogFilterDelegate
import ru.vladislavsumin.feature.logViewer.domain.logs.delegates.search.LogSearchDelegate
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractor
import ru.vladislavsumin.feature.logViewer.domain.proguard.ProguardInteractorImpl
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.Notification
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import java.nio.file.Path
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

/**
 * **Внимание, данный interactor является stateful.**
 */
interface LogsInteractor {
    /**
     * Возвращает текущий статус загрузки логов.
     */
    fun observeLoadingStatus(): StateFlow<LoadingStatus>

    fun observeMappingStatus(): StateFlow<MappingStatus>

    suspend fun detachMapping()
    suspend fun attachMapping(path: Path)

    /**
     * Строит "Индекс" (результат фильтрации и последующего поиска) на основе [filter] и [search].
     */
    fun observeLogIndex(
        filter: Flow<FilterRequest>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress>

    /**
     * Статус загрузки логов.
     *
     * - [LoadingLogs] логи загружаются
     * - [DeobfuscateLogs] obfuscated логи загружены и с ними уже можно работать, происходит преобразование логов.
     * - [Loaded] логи полностью загружены и обработаны.
     */
    sealed interface LoadingStatus {
        data object LoadingLogs : LoadingStatus
        data object DeobfuscateLogs : LoadingStatus
        data class Loaded(val isDeobfuscated: Boolean) : LoadingStatus
    }

    sealed interface MappingStatus {
        data object NotAttached : MappingStatus
        data object Attached : MappingStatus
    }
}

// TODO тут нужно оптимизировать количество копирований списка, а так же equals проверки.
class LogsInteractorImpl(
    private val scope: CoroutineScope,
    private val dispatchers: VsDispatchers,
    private val logPath: Path,
    private val logParserProvider: LogParserProvider,
    private val notificationsUiInteractor: NotificationsUiInteractor,
    proguardInteractor: ProguardInteractor?,
) : LogsInteractor {
    private val logs = MutableStateFlow(ClearLogState(emptyList(), null))
    private val loadingStatus = MutableStateFlow<LogsInteractor.LoadingStatus>(LogsInteractor.LoadingStatus.LoadingLogs)
    private val proguardState = MutableStateFlow(proguardInteractor)

    private val filterDelegate = LogFilterDelegate(logs)
    private val searchDelegate = LogSearchDelegate()

    init {
        loadLogs()
    }

    @Suppress("LongMethod") // TODO разгрести эту помойку на костылях
    private fun loadLogs() {
        scope.launch(dispatchers.IO) {
            proguardState.collectLatest { proguard ->
                if (proguard == null &&
                    (loadingStatus.value as? LogsInteractor.LoadingStatus.Loaded)?.isDeobfuscated == false
                ) {
                    return@collectLatest
                }

                loadingStatus.value = LogsInteractor.LoadingStatus.LoadingLogs
                logs.value = ClearLogState(emptyList(), null)

                val obfuscatedLogs = logParserProvider.getLogParser().parseLog(logPath)
                val runIdIndexes = logParserProvider.getRunIdParser()?.provideRunIdMeta(obfuscatedLogs)
                    ?.toRunIdInfo(obfuscatedLogs)

                logs.value = ClearLogState(
                    logs = obfuscatedLogs.toLogRecords(),
                    runIdIndexes,
                )

                // TODO ну парсим тут чего уж там, все равно говнокод
                if (proguard != null) {
                    loadingStatus.value = LogsInteractor.LoadingStatus.DeobfuscateLogs
                    val warmup = proguard.warmup()
                    if (warmup.isSuccess) {
                        val deobfuscated = obfuscatedLogs.parallelStream()
                            .map { log ->
                                // Обрабатываем теги
                                val deobfuscatedTag = proguard.deobfuscateClass(log.raw.substring(log.tag))
                                if (deobfuscatedTag != null) {
                                    log.copyTag(deobfuscatedTag)
                                } else {
                                    log
                                }
                            }
                            .map { log ->
                                // Обрабатываем stacktrace
                                if (log.lines > 2 && log.raw.lines().any { it.startsWith("\tat ") }) {
                                    val newMessage = proguard.deobfuscateStack(log.raw.substring(log.message))
                                    log.copy(
                                        raw = log.raw.replaceRange(log.message, newMessage),
                                        message = IntRange(
                                            log.message.first,
                                            log.message.first + newMessage.length - 1,
                                        ),
                                    )
                                } else {
                                    log
                                }
                            }
                            .toList()
                        logs.value = ClearLogState(
                            deobfuscated.toLogRecords(),
                            runIdIndexes,
                        )
                        loadingStatus.value = LogsInteractor.LoadingStatus.Loaded(true)
                    } else {
                        notificationsUiInteractor.showNotification(
                            Notification(
                                "Failed to loading mapping",
                                Notification.Servility.Error,
                            ),
                        )
                        LogLogger.e(warmup.exceptionOrNull()!!) { "Failed to loading mapping" }
                        loadingStatus.value = LogsInteractor.LoadingStatus.Loaded(false)
                        proguardState.value = null
                    }
                } else {
                    loadingStatus.value = LogsInteractor.LoadingStatus.Loaded(false)
                }
            }
        }
    }

    override fun observeMappingStatus(): StateFlow<LogsInteractor.MappingStatus> = proguardState.mapState {
        if (it == null) {
            LogsInteractor.MappingStatus.NotAttached
        } else {
            LogsInteractor.MappingStatus.Attached
        }
    }

    override suspend fun detachMapping() {
        proguardState.value = null
    }

    override suspend fun attachMapping(path: Path) {
        proguardState.value = ProguardInteractorImpl(path)
    }

    override fun observeLoadingStatus(): StateFlow<LogsInteractor.LoadingStatus> = loadingStatus

    override fun observeLogIndex(
        filter: Flow<FilterRequest>,
        search: Flow<SearchRequest>,
    ): Flow<LogIndexProgress> = channelFlow {
        // Если этот кеш не null, то в нем содержаться актуальные или прошлые результаты поиска
        var searchCache: LogIndex?

        filterDelegate.createFilterProgressFlow(filter).collectLatest { filterProgress ->
            // При изменении данных фильтра всегда обнуляем кеш.
            searchCache = null

            val logs = filterProgress.logs
            search.collectLatest { search ->
                val isSearch = search.search.isNotEmpty()

                // Если новый поисковый запрос пуст, то старый кеш нам больше не нужен.
                if (!isSearch) searchCache = null

                // Сразу отправляем результаты поиска + старый кеш далее
                send(
                    element = LogIndexProgress(
                        isFilteringNow = filterProgress.isFilteringNow,
                        isSearchingNow = isSearch,
                        lastSuccessIndex = searchCache ?: LogIndex(
                            logs = logs,
                            searchIndex = LogIndex.SearchIndex.NoSearch,
                            totalLogRecords = filterProgress.totalLogRecords,
                            runIdOrders = filterProgress.runIdOrders,
                        ),
                    ),
                )

                // Проводим новый поиск
                if (!filterProgress.isFilteringNow && isSearch) {
                    val logIndex = searchDelegate.searchLogs(
                        logs,
                        search,
                        filterProgress.totalLogRecords,
                        filterProgress.runIdOrders,
                    )
                    searchCache = logIndex
                    send(
                        element = LogIndexProgress(
                            isFilteringNow = false,
                            isSearchingNow = false,
                            lastSuccessIndex = logIndex,
                        ),
                    )
                }
            }
        }
    }.flowOn(dispatchers.Default)
}

data class ClearLogState(
    val logs: List<LogRecord>,
    val runIdIndexes: List<RunIdInfo>?,
)

private fun RawLogRecord.toLogRecord(order: Int) = LogRecord(
    order = order,
    raw = raw,
    time = time,
    timeInstant = timeInstant,
    level = level,
    processId = processId,
    thread = thread,
    tag = tag,
    message = message,
    searchHighlight = null,
    logLevel = logLevel,
)

private fun List<RawLogRecord>.toLogRecords(): List<LogRecord> =
    mapIndexed { index, record -> record.toLogRecord(index) }

private fun List<RawRunIdInfo>.toRunIdInfo(
    obfuscatedLogs: List<RawLogRecord>,
): List<RunIdInfo> = mapIndexed { index, info ->
    val endIndex = if (index + 1 < size) {
        this[index + 1].startIndex - 1
    } else {
        Int.MAX_VALUE
    }

    val startTime = obfuscatedLogs[info.startIndex].timeInstant
    val endTime = obfuscatedLogs[if (endIndex == Int.MAX_VALUE) obfuscatedLogs.size - 1 else endIndex].timeInstant
    val duration = ChronoUnit.SECONDS.between(startTime, endTime).seconds

    RunIdInfo(
        orderRange = IntRange(info.startIndex, endIndex),
        meta = info.meta + ("duration" to duration.toString()),
    )
}
