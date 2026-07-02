package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor

interface RootScreenFactory {
    fun create(
        windowTitleInteractor: WindowTitleInteractor?,
        globalHotkeyManager: GlobalHotkeyManager,
        context: ComponentContext,
    ): Screen
}
