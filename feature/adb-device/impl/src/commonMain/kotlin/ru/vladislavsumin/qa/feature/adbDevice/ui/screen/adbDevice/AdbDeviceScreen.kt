package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

@GenerateFactory(AdbDeviceScreenFactory::class)
internal class AdbDeviceScreen(
    viewModelFactory: DeviceControlViewModelFactory,
    private val params: AdbDeviceScreenParams,
    context: ComponentContext,
) : Screen(context),
    TabSupport {

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(name = params.deviceName),
    )
    private val viewModel: DeviceControlViewModel = viewModel { viewModelFactory.create(params) }

    @Composable
    override fun Render(modifier: Modifier) = AdbDeviceScreenContent(
        deviceName = params.deviceName,
        viewModel,
        modifier,
    )
}
