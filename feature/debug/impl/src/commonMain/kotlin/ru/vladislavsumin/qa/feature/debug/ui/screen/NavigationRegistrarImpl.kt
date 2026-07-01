package ru.vladislavsumin.qa.feature.debug.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.debug.ui.screen.debug.DebugScreenFactory
import ru.vladislavsumin.qa.feature.debug.ui.screen.debug.DebugScreenParams

internal class NavigationRegistrarImpl(private val debugScreenFactory: DebugScreenFactory) : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            factory = debugScreenFactory,
            defaultParams = DebugScreenParams,
            description = "Экран отладки с графом навигации",
        )
    }
}
