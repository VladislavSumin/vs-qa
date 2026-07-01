package ru.vladislavsumin.qa.feature.multiWindow.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot.MultiWindowNavigationHost
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot.MultiWindowRootScreenFactory
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot.MultiWindowRootScreenParams
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowNavigationHost
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenFactory
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenParams
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenParams

internal class NavigationRegistrarImpl(
    private val multiWindowRootScreenFactory: MultiWindowRootScreenFactory,
    private val windowScreenFactory: WindowScreenFactory,
) : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            factory = multiWindowRootScreenFactory,
            defaultParams = MultiWindowRootScreenParams,
            description = "MultiWindow root screen",
            navigationHosts = {
                MultiWindowNavigationHost opens setOf(
                    WindowScreenParams::class,
                )
            },
        )
        registerScreen(
            factory = windowScreenFactory,
            defaultParams = WindowScreenParams("default"),
            description = "MultiWindow root screen",
            navigationHosts = {
                WindowNavigationHost opens setOf(
                    RootScreenParams::class,
                )
            },
        )
    }
}
