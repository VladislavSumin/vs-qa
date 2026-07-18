package ru.vladislavsumin.feature.logViewer.ui.component.dragAndDropOverlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.ui.dragAndDrop.DragAndDropOverlay
import ru.vladislavsumin.core.ui.dragAndDrop.rememberDragAndDropFilesTarget
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.Res
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_drop_logs
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_drop_mapping
import java.nio.file.Path

@Composable
internal fun DragAndDropOverlayContent(
    isMappingSupported: Boolean,
    onMappingPathsSelected: (List<Path>) -> Unit,
    onLogPathsSelected: (List<Path>) -> Unit,
    modifier: Modifier,
) {
    DragAndDropOverlay(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DropTargetCard(
                text = stringResource(Res.string.log_viewer_drop_logs),
                onPathSelected = onLogPathsSelected,
            )

            if (isMappingSupported) {
                DropTargetCard(
                    text = stringResource(Res.string.log_viewer_drop_mapping),
                    onPathSelected = onMappingPathsSelected,
                )
            }
        }
    }
}

@Composable
private fun DropTargetCard(text: String, onPathSelected: (List<Path>) -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    val borderColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.rememberDragAndDropFilesTarget(
            onDropped = onPathSelected,
            onHoveredStateChanged = { isHovered = it },
        ),
        border = if (isHovered) BorderStroke(2.dp, borderColor) else null,
    ) {
        Box(
            Modifier.defaultMinSize(
                minWidth = 300.dp,
                minHeight = 200.dp,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text)
        }
    }
}
