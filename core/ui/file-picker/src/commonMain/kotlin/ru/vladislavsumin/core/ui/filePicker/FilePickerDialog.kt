package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable
import java.nio.file.Path

@Composable
expect fun FilePickerDialog(
    mimeType: String = "*/*",
    multiple: Boolean = false,
    onCloseRequest: (result: List<Path>) -> Unit,
)
