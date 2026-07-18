package ru.vladislavsumin.feature.logViewer.ui.component.dragAndDropOverlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import java.nio.file.Path

internal class DragAndDropOverlayComponent(
    private val isMappingSupported: Boolean,
    private val onMappingPathsSelected: (List<Path>) -> Unit,
    private val onLogPathsSelected: (List<Path>) -> Unit,
    context: ComponentContext,
) : Component(context),
    ComposeComponent {
    @Composable
    override fun Render(modifier: Modifier) {
        DragAndDropOverlayContent(isMappingSupported, onMappingPathsSelected, onLogPathsSelected, modifier)
    }
}
