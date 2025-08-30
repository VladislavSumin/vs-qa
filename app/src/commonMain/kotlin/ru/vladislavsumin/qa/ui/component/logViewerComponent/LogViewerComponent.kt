package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

internal class LogViewerComponent(context: ComponentContext) : Component(context), ComposeComponent {
    val viewModel = viewModel { LogViewerViewModel() }

    @Composable
    override fun Render(modifier: Modifier) = LogViewerContent(viewModel, modifier)
}
