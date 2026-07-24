package ru.vladislavsumin.feature.logsDashboard.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.feature.logsDashboard.ui.screen.logsDashboard.LogsDashboardScreenParams

internal class NavigationRegistrarImpl : NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen<LogsDashboardScreenParams>(
            description = "Logs dashboard screen",
        )
    }
}
