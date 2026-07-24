package ru.vladislavsumin.feature.logsDashboard.ui.screen.logsDashboard

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen

interface LogsDashboardScreenFactory {
    fun create(params: LogsDashboardScreenParams, context: ComponentContext): Screen
}
