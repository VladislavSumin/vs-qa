package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onRightClick(onRightClick: (Offset) -> Unit): Modifier =
    this.onPointerEvent(PointerEventType.Press) { event ->
        if (event.button == PointerButton.Secondary) {
            event.changes.forEach { it.consume() }
            onRightClick(event.changes.first().position)
        }
    }
