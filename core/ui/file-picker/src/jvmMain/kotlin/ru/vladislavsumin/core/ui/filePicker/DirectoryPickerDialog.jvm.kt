package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import javax.swing.JFileChooser

@Composable
actual fun DirectoryPickerDialog(title: String, onCloseRequest: (path: String?) -> Unit) {
    LaunchedEffect(Unit) {
        val chooser = JFileChooser(File(System.getProperty("user.home"))).apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }
        val result = chooser.showOpenDialog(null)
        val path = if (result == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else {
            null
        }
        onCloseRequest(path)
    }
}
