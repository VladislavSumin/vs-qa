package ru.vladislavsumin.core.ui.dragAndDrop

import androidx.compose.ui.draganddrop.DragAndDropEvent
import java.nio.file.Path

expect fun DragAndDropEvent.getPath(): Path