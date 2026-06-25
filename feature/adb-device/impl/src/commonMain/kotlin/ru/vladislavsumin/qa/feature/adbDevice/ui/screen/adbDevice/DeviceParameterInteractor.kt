package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

internal interface DeviceParameterInteractor {
    suspend fun readParameters(deviceName: String): List<DeviceParameter>
    suspend fun setToggle(deviceName: String, parameterId: String, value: Boolean)
}
