package ru.vladislavsumin.feature.logViewer.ui.component.dragAndDropOverlay

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.dragAndDrop.rememberDragAndDropFileTarget
import java.nio.file.Path

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragAndDropOverlayContent(
    onMappingPathSelected: (Path) -> Unit,
    onLogPathSelected: (Path) -> Unit,
    modifier: Modifier,
) {
    var isShowDragAndDropActions by remember { mutableStateOf(false) }
    val rootDragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isShowDragAndDropActions = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isShowDragAndDropActions = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean = false
        }
    }
    Row(
        modifier
            // Костыль, иначе не сможем получить событие drag && drop.
            .let { if (isShowDragAndDropActions) it.fillMaxSize() else it.size(0.dp) }
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = rootDragAndDropTarget,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Card(modifier = Modifier.rememberDragAndDropFileTarget(onLogPathSelected)) {
            Box(
                Modifier.defaultMinSize(
                    minWidth = 300.dp,
                    minHeight = 200.dp,
                ),
            ) { Text(text = "Drop logs here", modifier = Modifier.align(Alignment.Center)) }
        }

        Card(modifier = Modifier.rememberDragAndDropFileTarget(onMappingPathSelected)) {
            Box(
                Modifier.defaultMinSize(
                    minWidth = 300.dp,
                    minHeight = 200.dp,
                ),
            ) { Text(text = "Drop mapping here", Modifier.align(Alignment.Center)) }
        }
    }
}
