package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory(AdbDeviceListComponentFactory::class)
internal class AdbDeviceListComponent(
    viewModelFactory: AdbDeviceListViewModelFactory,
    context: ComponentContext,
) : Component(context), ComposeComponent {

    private val viewModel = viewModel { viewModelFactory.create() }

    @Composable
    override fun Render(modifier: Modifier) = AdbDeviceListContent(viewModel, modifier)
}
