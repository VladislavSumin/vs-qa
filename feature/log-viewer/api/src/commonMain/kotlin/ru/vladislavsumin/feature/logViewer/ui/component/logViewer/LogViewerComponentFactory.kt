package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import java.nio.file.Path

interface LogViewerComponentFactory {
    fun create(
        logPath: Path,
        mappingPath: Path?,
        context: ComponentContext,
    ): ComposeComponent
}
