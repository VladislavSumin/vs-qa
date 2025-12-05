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
fun Modifier.rememberDragAndDropFileTarget(onDropped: (Path) -> Unit): Modifier {
    val target = remember(onDropped) {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                onDropped(event.getPath())
                return true
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = target,
    )
}