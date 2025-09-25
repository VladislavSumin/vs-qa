package ru.vladislavsumin.feature.logViewer.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams

internal class NavigationRegistrarImpl : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen<LogViewerScreenParams>(
            description = "Log viewer screen",
        )
    }
}
