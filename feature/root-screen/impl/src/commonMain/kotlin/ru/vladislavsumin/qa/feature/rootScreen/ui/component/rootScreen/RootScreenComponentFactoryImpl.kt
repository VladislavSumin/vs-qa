package ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import java.nio.file.Path

internal class RootScreenComponentFactoryImpl(
    private val logViewerComponentFactory: LogViewerComponentFactory,
    private val bottomBarComponentFactory: BottomBarComponentFactory,
) : RootScreenComponentFactory {
    override fun create(
        logPath: Path,
        mappingPath: Path?,
        context: ComponentContext,
    ): ComposeComponent {
        return RootScreenComponent(
            logViewerComponentFactory,
            bottomBarComponentFactory,
            logPath,
            mappingPath,
            context,
        )
    }
}
