package ru.vladislavsumin.core.ui.dragAndDrop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.awtTransferable
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.nio.file.Path

@OptIn(ExperimentalComposeUiApi::class)
actual fun DragAndDropEvent.getPath(): Path {
    val files = this.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
    return files.single().toPath()
}
