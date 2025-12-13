package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory(AdbDeviceListComponentFactory::class)
internal class AdbDeviceListComponent(context: ComponentContext) : Component(context), ComposeComponent {
    @Composable
    override fun Render(modifier: Modifier) {
        Box(modifier) {
            Text(
                text = "Adb feature.\nIn development",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}
