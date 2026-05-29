package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

internal sealed interface AdbDeviceListViewState {
    data class DeviceList(
        val devices: List<Device>,
    ) : AdbDeviceListViewState

    data object Error : AdbDeviceListViewState

    data class Device(
        val name: String,
        val status: String,
        val statusColor: StatusColor,
    ) {
        enum class StatusColor {
            Red,
            Yellow,
            Green,
        }
    }

    companion object {
        val STUB = DeviceList(devices = emptyList())
    }
}
