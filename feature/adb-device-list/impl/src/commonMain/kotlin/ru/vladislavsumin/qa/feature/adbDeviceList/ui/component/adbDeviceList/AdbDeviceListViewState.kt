package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

internal data class AdbDeviceListViewState(
    val devices: List<Device>,
) {
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
        val STUB = AdbDeviceListViewState(devices = emptyList())
    }
}
