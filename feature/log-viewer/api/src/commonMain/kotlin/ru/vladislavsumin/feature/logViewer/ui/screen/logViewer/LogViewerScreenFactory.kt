package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor

interface LogViewerScreenFactory {
    fun create(
        bottomBarUiInteractor: BottomBarUiInteractor,
        params: LogViewerScreenParams,
        context: ComponentContext,
    ): Screen
}
