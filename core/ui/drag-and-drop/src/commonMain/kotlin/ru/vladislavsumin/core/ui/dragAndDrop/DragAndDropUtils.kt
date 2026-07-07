package ru.vladislavsumin.core.ui.dragAndDrop

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import java.nio.file.Path

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.rememberDragAndDropFilesTarget(
    onDropped: (List<Path>) -> Unit,
    onHoveredStateChanged: ((Boolean) -> Unit)? = null,
): Modifier {
    val target = remember(onDropped, onHoveredStateChanged) {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) {
                onHoveredStateChanged?.invoke(true)
            }

            override fun onExited(event: DragAndDropEvent) {
                onHoveredStateChanged?.invoke(false)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                onHoveredStateChanged?.invoke(false)
                onDropped(event.getPaths())
                return true
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = target,
    )
}
