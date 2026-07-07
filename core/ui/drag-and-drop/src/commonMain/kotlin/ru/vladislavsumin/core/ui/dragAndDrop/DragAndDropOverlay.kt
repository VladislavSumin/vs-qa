package ru.vladislavsumin.core.ui.dragAndDrop

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.unit.dp

/**
 * Оверлей для приёма файлов drag-and-drop.
 *
 * Отслеживает начало и конец drag-сессии через [DragAndDropTarget.onStarted]/[onEnded]
 * и показывает замаскированные в [content] drop-зоны только на время активного переноса.
 *
 * Drop-зоны всегда присутствуют в композиции — иначе Compose не успевает их зарегистрировать
 * к моменту drop. Вместо условного добавления используется переключение размера:
 * - [fillMaxSize], когда drag активен — оверлей виден и принимает файлы;
 * - [size](0.dp), когда drag неактивен — оверлей невидим и не перехватывает клики,
 *   но его drop-таргеты уже зарегистрированы в дереве.
 *
 * @param modifier применяется к контейнеру оверлея (например, для отступов)
 * @param content композиция drop-зон — с [Modifier.rememberDragAndDropFilesTarget] и т.п.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DragAndDropOverlay(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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
    Box(
        modifier = modifier
            .let { if (isShowDragAndDropActions) it.fillMaxSize() else it.size(0.dp) }
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = rootDragAndDropTarget,
            ),
    ) {
        content()
    }
}
