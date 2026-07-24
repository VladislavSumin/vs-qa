package ru.vladislavsumin.feature.logsDashboard.ui.screen.logsDashboard

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen

internal class LogsDashboardScreenFactoryImpl : LogsDashboardScreenFactory {
    override fun create(params: LogsDashboardScreenParams, context: ComponentContext): Screen =
        LogsDashboardScreen(context)
}
