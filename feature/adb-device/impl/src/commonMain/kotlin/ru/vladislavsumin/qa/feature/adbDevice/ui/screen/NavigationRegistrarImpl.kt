package ru.vladislavsumin.qa.feature.adbDevice.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenParams

internal class NavigationRegistrarImpl : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen<AdbDeviceScreenParams>(
            description = "ADB device screen",
        )
    }
}
