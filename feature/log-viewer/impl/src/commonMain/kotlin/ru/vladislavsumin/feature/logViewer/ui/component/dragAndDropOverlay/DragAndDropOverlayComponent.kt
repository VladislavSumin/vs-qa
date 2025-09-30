package ru.vladislavsumin.feature.logViewer.ui.component.dragAndDropOverlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import java.nio.file.Path

internal class DragAndDropOverlayComponent(
    private val onMappingPathSelected: (Path) -> Unit,
    private val onLogPathSelected: (Path) -> Unit,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    @Composable
    override fun Render(modifier: Modifier) {
        DragAndDropOverlayContent(onMappingPathSelected, onLogPathSelected, modifier)
    }
}
