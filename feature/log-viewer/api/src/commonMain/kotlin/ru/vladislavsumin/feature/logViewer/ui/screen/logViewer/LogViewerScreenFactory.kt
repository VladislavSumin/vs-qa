package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor

interface LogViewerScreenFactory {
    fun create(
        bottomBarUiInteractor: BottomBarUiInteractor,
        params: LogViewerScreenParams,
        intents: ReceiveChannel<LogViewerScreenIntent>,
        context: ComponentContext,
    ): Screen
}
