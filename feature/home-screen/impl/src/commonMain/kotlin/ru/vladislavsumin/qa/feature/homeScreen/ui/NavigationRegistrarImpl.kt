package ru.vladislavsumin.qa.feature.homeScreen.ui

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenFactory
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams

internal class NavigationRegistrarImpl(
    private val homeScreenFactory: HomeScreenFactory,
) : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            factory = homeScreenFactory,
            defaultParams = HomeScreenParams,
            description = "Home screen",
        )
    }
}
