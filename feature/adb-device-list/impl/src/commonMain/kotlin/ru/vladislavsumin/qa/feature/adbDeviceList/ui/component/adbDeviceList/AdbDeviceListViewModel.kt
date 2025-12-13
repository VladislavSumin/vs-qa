package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.runtime.Stable
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory
@Stable
internal class AdbDeviceListViewModel(
    private val adbClient: AdbClient,
) : ViewModel() {
    val state = adbClient.observeDevices()
        .stateIn(emptyList())
}
