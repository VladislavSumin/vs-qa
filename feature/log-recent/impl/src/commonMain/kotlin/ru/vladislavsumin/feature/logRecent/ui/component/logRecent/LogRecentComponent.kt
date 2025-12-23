package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import java.nio.file.Path

@GenerateFactory(LogRecentComponentFactory::class)
internal class LogRecentComponent(
    private val onOpenLogRecent: (path: Path) -> Unit,
    logRecentViewModelFactory: LogRecentViewModelFactory,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel = viewModel { logRecentViewModelFactory.create() }

    @Composable
    override fun Render(modifier: Modifier) = LogRecentContent(onOpenLogRecent, viewModel, modifier)
}
