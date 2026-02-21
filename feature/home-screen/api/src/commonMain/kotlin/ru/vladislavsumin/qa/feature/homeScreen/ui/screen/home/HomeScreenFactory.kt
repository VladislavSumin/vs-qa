package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor

interface HomeScreenFactory {
    fun create(
        notificationsUiInteractor: NotificationsUiInteractor,
        params: HomeScreenParams,
        context: ComponentContext,
    ): Screen
}
