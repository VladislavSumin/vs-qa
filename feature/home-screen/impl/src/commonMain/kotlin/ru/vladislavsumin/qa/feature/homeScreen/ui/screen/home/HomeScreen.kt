package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenParams
import ru.vladislavsumin.qa.feature.adbDeviceList.domain.AdbFeatureAvailabilityInteractor
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListComponentFactory
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

@GenerateFactory(HomeScreenFactory::class)
@Suppress("UnusedPrivateProperty") // TODO доработать генератор фабрик?
internal class HomeScreen(
    viewModelFactory: HomeScreenViewModelFactory,
    logRecentComponentFactory: LogRecentComponentFactory,
    adbFeatureAvailabilityInteractor: AdbFeatureAvailabilityInteractor,
    adbDeviceListComponentFactory: AdbDeviceListComponentFactory,
    notificationsUiInteractor: NotificationsUiInteractor,
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
        onOpenLogRecent = { path -> navigator.open(LogViewerScreenParams(path)) },
        context = context.childContext("log-recent"),
    )

    private val adbDeviceListComponent = if (adbFeatureAvailabilityInteractor.isAvailable) {
        adbDeviceListComponentFactory.create(
            onDeviceClick = { deviceName -> navigator.open(AdbDeviceScreenParams(deviceName)) },
            context = context.childContext("adb-list-component"),
        )
    } else {
        null
    }

    @Composable
    override fun Render(modifier: Modifier) = HomeScreenContent(
        viewModel = viewModel,
        logRecentComponent = logRecentComponent,
        adbDeviceListComponent = adbDeviceListComponent,
    )
}
