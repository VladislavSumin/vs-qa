package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.Res
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_dump_failed
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_dump_progress
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerSource
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenParams
import ru.vladislavsumin.qa.feature.adbDeviceList.domain.AdbFeatureAvailabilityInteractor
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListComponentFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarText
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import ru.vladislavsumin.qa.feature.deviceLogDump.domain.DeviceLogDumpInteractor
import ru.vladislavsumin.qa.feature.homeScreen.HomeLogger
import ru.vladislavsumin.qa.feature.legalInfo.ui.screen.legalInfo.LegalInfoScreenParams
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenParams
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.Notification
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationText
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import ru.vladislavsumin.qa.feature.settings.ui.screen.settings.SettingsScreenParams
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport
import kotlin.random.Random

@GenerateFactory(HomeScreenFactory::class)
@Suppress("UnusedPrivateProperty") // TODO доработать генератор фабрик?
internal class HomeScreen(
    viewModelFactory: HomeScreenViewModelFactory,
    logRecentComponentFactory: LogRecentComponentFactory,
    adbFeatureAvailabilityInteractor: AdbFeatureAvailabilityInteractor,
    adbDeviceListComponentFactory: AdbDeviceListComponentFactory,
    deviceLogDumpInteractor: DeviceLogDumpInteractor,
    notificationsUiInteractor: NotificationsUiInteractor,
    bottomBarUiInteractor: BottomBarUiInteractor,
    globalHotkeyManager: GlobalHotkeyManager,
    params: HomeScreenParams,
    context: ComponentContext,
) : Screen(context),
    TabSupport {
    private val viewModel: HomeScreenViewModel = viewModel { viewModelFactory.create() }

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(icon = Icons.Default.Home, allowClose = false),
    )
    private val logRecentComponent = logRecentComponentFactory.create(
        notificationsUiInteractor = notificationsUiInteractor,
        onOpenLogRecent = { path, openInNewWindow ->
            HomeLogger.d { "Open log recent, path=$path, openInNewWindow=$openInNewWindow" }
            if (openInNewWindow) {
                navigator.open(
                    screenParams = LogViewerScreenParams(path),
                    hints = listOf(WindowScreenParams(Random.nextLong().toString())),
                )
            } else {
                navigator.open(LogViewerScreenParams(path))
            }
        },
        context = context.childContext("log-recent"),
    )

    private val adbDeviceListComponent = if (adbFeatureAvailabilityInteractor.isAvailable) {
        adbDeviceListComponentFactory.create(
            onDeviceClick = { deviceName -> navigator.open(AdbDeviceScreenParams(deviceName)) },
            onDumpLogsClick = { deviceName ->
                scope.launch {
                    val progressJob = launch {
                        bottomBarUiInteractor.showProgressBar(
                            BottomBarText(Res.string.home_dump_progress, listOf(deviceName)),
                        )
                    }
                    try {
                        deviceLogDumpInteractor.dumpLogs(deviceName)
                            .onSuccess { path -> navigator.open(LogViewerScreenParams(path)) }
                            .onFailure { error ->
                                HomeLogger.e(error) { "Dump failed for $deviceName" }
                                notificationsUiInteractor.showNotification(
                                    Notification(
                                        text = NotificationText(
                                            Res.string.home_dump_failed,
                                            listOf(deviceName, error.message.orEmpty()),
                                        ),
                                        servility = Notification.Servility.Error,
                                    ),
                                )
                            }
                    } finally {
                        progressJob.cancel()
                    }
                }
            },
            onViewLogcatClick = { deviceName ->
                navigator.open(LogViewerScreenParams(LogViewerSource.DeviceLogcat(deviceName)))
            },
            context = context.childContext("adb-list-component"),
        )
    } else {
        null
    }

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            globalHotkeyManager.subscribe(
                KeyModifier.Command + Key.O to {
                    val wasShowing = viewModel.state.value
                    viewModel.onClickOpen()
                    !wasShowing
                },
            )
        }
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = HomeScreenContent(
        viewModel = viewModel,
        onLogPathsSelected = viewModel::onDragAndDropLogsFiles,
        logRecentComponent = logRecentComponent,
        adbDeviceListComponent = adbDeviceListComponent,
        onOpenLegalInfo = { navigator.open(LegalInfoScreenParams) },
        onOpenSettings = { navigator.open(SettingsScreenParams) },
    )
}
