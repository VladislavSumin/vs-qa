package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.ui.component.memoryIndicatorComponent.MemoryIndicatorComponent
import java.nio.file.Path

internal class LogViewerComponent(
    logPath: Path,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel = viewModel { LogViewerViewModel(logPath) }
    private val memoryIndicator: ComposeComponent = MemoryIndicatorComponent(context.childContext("memory-indicator"))

    @Composable
    override fun Render(modifier: Modifier) = LogViewerContent(viewModel, memoryIndicator, modifier)
}
