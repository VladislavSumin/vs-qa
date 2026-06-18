package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.ScreenParams

@Serializable
data class AdbDeviceScreenParams(val deviceName: String) : ScreenParams
