package ru.vladislavsumin.qa.feature.settings.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.settings.ui.screen.settings.SettingsScreenFactory
import ru.vladislavsumin.qa.feature.settings.ui.screen.settings.SettingsScreenParams

internal class NavigationRegistrarImpl(private val settingsScreenFactory: SettingsScreenFactory) :
    NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            factory = settingsScreenFactory,
            defaultParams = SettingsScreenParams,
            description = "Настройки",
        )
    }
}
