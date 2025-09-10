package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator.MemoryIndicatorComponentFactory
import java.nio.file.Path

internal class LogViewerComponent(
    logPath: Path,
    mappingPath: Path?,
    memoryIndicatorComponentFactory: MemoryIndicatorComponentFactory,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel = viewModel { LogViewerViewModel(logPath, mappingPath) }
    private val memoryIndicator: ComposeComponent =
        memoryIndicatorComponentFactory.create(context.childContext("memory-indicator"))

    @Composable
    override fun Render(modifier: Modifier) = LogViewerContent(viewModel, memoryIndicator, modifier)
}
