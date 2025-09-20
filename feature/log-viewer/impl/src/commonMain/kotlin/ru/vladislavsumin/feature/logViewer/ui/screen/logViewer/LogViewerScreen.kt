package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor

internal class LogViewerScreen(
    componentFactory: LogViewerComponentFactory,
    bottomBarUiInteractor: BottomBarUiInteractor,
    params: LogViewerScreenParams,
    context: ComponentContext,
) : Screen(context) {
    private val component = componentFactory.create(
        logPath = params.logPath,
        mappingPath = params.mappingPath,
        bottomBarUiInteractor = bottomBarUiInteractor,
        context = context.childContext("log-viewer"),
    )

    @Composable
    override fun Render(modifier: Modifier) {
        component.Render(modifier)
    }
}

class LogViewerScreenFactoryImpl(
    private val componentFactory: LogViewerComponentFactory,
) : LogViewerScreenFactory {
    override fun create(
        bottomBarUiInteractor: BottomBarUiInteractor,
        params: LogViewerScreenParams,
        context: ComponentContext,
    ): Screen {
        return LogViewerScreen(
            componentFactory = componentFactory,
            bottomBarUiInteractor = bottomBarUiInteractor,
            params = params,
            context = context,
        )
    }
}
