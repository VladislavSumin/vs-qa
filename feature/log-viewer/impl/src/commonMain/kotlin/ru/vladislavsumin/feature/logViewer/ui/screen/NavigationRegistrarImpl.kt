package ru.vladislavsumin.feature.logViewer.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams

internal class NavigationRegistrarImpl : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        // TODO подумать над api навигации, кажется тут лишний generic
        registerScreen<LogViewerScreenParams, NoIntent, Screen>(
            factory = null,
            defaultParams = null as LogViewerScreenParams?,
            description = "Log viewer screen",
        )
    }
}
