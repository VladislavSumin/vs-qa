package ru.vladislavsumin.qa.feature.rootScreen.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenFactory
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenParams
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.TabNavigationHost

internal class NavigationRegistrarImpl(
    private val rootScreenFactory: RootScreenFactory,
) : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            factory = rootScreenFactory,
            defaultParams = RootScreenParams,
            description = "Root screen",
            navigationHosts = {
                TabNavigationHost opens setOf(
                    HomeScreenParams::class,
                    LogViewerScreenParams::class,
                )
            },
        )
    }
}
