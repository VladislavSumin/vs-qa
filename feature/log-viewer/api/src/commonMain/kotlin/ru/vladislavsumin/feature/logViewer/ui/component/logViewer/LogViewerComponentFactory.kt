package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import java.nio.file.Path

interface LogViewerComponentFactory {
    fun create(
        logPath: Path,
        mappingPath: Path?,
        bottomBarUiInteractor: BottomBarUiInteractor,
        context: ComponentContext,
    ): ComposeComponent
}
