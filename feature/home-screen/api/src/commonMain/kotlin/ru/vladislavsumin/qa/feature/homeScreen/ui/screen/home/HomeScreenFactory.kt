package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor

interface HomeScreenFactory {
    fun create(
        notificationsUiInteractor: NotificationsUiInteractor,
        bottomBarUiInteractor: BottomBarUiInteractor,
        globalHotkeyManager: GlobalHotkeyManager,
        params: HomeScreenParams,
        context: ComponentContext,
    ): Screen
}
