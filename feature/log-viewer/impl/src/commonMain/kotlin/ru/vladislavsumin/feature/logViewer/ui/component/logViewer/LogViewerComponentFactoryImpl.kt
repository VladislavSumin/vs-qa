package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator.MemoryIndicatorComponentFactory
import java.nio.file.Path

internal class LogViewerComponentFactoryImpl(
    private val memoryIndicatorComponentFactory: MemoryIndicatorComponentFactory,
) : LogViewerComponentFactory {
    override fun create(logPath: Path, mappingPath: Path?, context: ComponentContext): ComposeComponent {
        return LogViewerComponent(logPath, mappingPath, memoryIndicatorComponentFactory, context)
    }
}
