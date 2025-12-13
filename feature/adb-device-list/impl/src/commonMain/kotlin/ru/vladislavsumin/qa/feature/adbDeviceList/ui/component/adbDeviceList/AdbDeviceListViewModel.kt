package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.core.adb.client.AdbClient.DeviceInfo.ConnectionStatus
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListViewState.Device.StatusColor

@GenerateFactory
@Stable
internal class AdbDeviceListViewModel(
    private val adbClient: AdbClient,
) : ViewModel() {
    val state: StateFlow<AdbDeviceListViewState> = adbClient.observeDevices()
        .map { deviceInfos ->
            val devices = deviceInfos.map { (name, status) ->
                AdbDeviceListViewState.Device(
                    name = name,
                    status = status.name,
                    statusColor = when (status) {
                        ConnectionStatus.Device -> StatusColor.Green
                        ConnectionStatus.Authorizing -> StatusColor.Yellow
                        ConnectionStatus.Offline -> StatusColor.Red
                    },
                )
            }

            AdbDeviceListViewState(devices)
        }
        .stateIn(AdbDeviceListViewState.STUB)
}
