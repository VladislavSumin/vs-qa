package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.adbDeviceList.domain.AdbFeatureAvailabilityInteractor
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListComponentFactory

@GenerateScreenFactory
internal class HomeScreen(
    viewModelFactory: HomeScreenViewModelFactory,
    adbFeatureAvailabilityInteractor: AdbFeatureAvailabilityInteractor,
    adbDeviceListComponentFactory: AdbDeviceListComponentFactory,
    context: ComponentContext,
) : Screen(context) {
    private val viewModel = viewModel { viewModelFactory.create() }

    private val adbDeviceListComponent = if (adbFeatureAvailabilityInteractor.isAvailable) {
        adbDeviceListComponentFactory.create(context.childContext("adb-list-component"))
    } else {
        null
    }

    @Composable
    override fun Render(modifier: Modifier) = HomeScreenContent(viewModel, adbDeviceListComponent)
}
