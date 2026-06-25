package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.screen.Screen

@GenerateFactory(AdbDeviceScreenFactory::class)
internal class AdbDeviceScreen(
    viewModelFactory: DeviceControlViewModelFactory,
    private val params: AdbDeviceScreenParams,
    context: ComponentContext,
) : Screen(context) {
    private val viewModel: DeviceControlViewModel = viewModel { viewModelFactory.create(params) }

    @Composable
    override fun Render(modifier: Modifier) = AdbDeviceScreenContent(
        deviceName = params.deviceName,
        viewModel,
        modifier,
    )
}
