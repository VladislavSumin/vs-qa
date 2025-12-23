package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.feature.adbDeviceList.domain.AdbFeatureAvailabilityInteractor
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListComponentFactory

@GenerateScreenFactory
internal class HomeScreen(
    viewModelFactory: HomeScreenViewModelFactory,
    logRecentComponentFactory: LogRecentComponentFactory,
    adbFeatureAvailabilityInteractor: AdbFeatureAvailabilityInteractor,
    adbDeviceListComponentFactory: AdbDeviceListComponentFactory,
    context: ComponentContext,
) : Screen(context) {
    private val viewModel = viewModel { viewModelFactory.create() }

    private val logRecentComponent = logRecentComponentFactory.create(
        onOpenLogRecent = { path -> navigator.open(LogViewerScreenParams(path)) },
        context = context.childContext("log-recent"),
    )

    private val adbDeviceListComponent = if (adbFeatureAvailabilityInteractor.isAvailable) {
        adbDeviceListComponentFactory.create(context.childContext("adb-list-component"))
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
