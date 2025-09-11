package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import java.nio.file.Path

internal class LogViewerComponent(
    logPath: Path,
    mappingPath: Path?,
    bottomBarUiInteractor: BottomBarUiInteractor,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel = viewModel { LogViewerViewModel(logPath, mappingPath, bottomBarUiInteractor) }

    @Composable
    override fun Render(modifier: Modifier) = LogViewerContent(viewModel, modifier)
}
