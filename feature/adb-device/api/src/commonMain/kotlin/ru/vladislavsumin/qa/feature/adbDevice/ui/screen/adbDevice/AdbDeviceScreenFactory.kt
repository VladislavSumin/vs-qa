package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen

interface AdbDeviceScreenFactory {
    fun create(params: AdbDeviceScreenParams, context: ComponentContext): Screen
}
