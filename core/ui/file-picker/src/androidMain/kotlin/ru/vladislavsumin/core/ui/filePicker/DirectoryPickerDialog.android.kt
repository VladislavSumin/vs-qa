package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun DirectoryPickerDialog(title: String, onCloseRequest: (path: String?) -> Unit) {
    LaunchedEffect(Unit) {
        onCloseRequest(null)
    }
}
