package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable

@Composable
expect fun DirectoryPickerDialog(title: String = "Choose directory", onCloseRequest: (path: String?) -> Unit)
