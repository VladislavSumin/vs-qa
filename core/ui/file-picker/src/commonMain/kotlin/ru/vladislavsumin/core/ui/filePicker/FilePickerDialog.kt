package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable
import java.nio.file.Path

@Composable
expect fun FilePickerDialog(onCloseRequest: (result: Path?) -> Unit)
