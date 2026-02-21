package ru.vladislavsumin.qa.feature.homeScreen.ui

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams

internal class NavigationRegistrarImpl : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            defaultParams = HomeScreenParams,
            description = "Home screen",
        )
    }
}
