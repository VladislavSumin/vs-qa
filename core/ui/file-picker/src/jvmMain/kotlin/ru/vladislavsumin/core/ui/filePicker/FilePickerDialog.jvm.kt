package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import kotlin.io.path.Path

@Composable
actual fun FilePickerDialog(
    mimeType: String,
    multiple: Boolean,
    onCloseRequest: (result: List<java.nio.file.Path>) -> Unit,
) = AwtWindow(
    create = {
        object : FileDialog(null as Frame?, "Choose a file", LOAD) {
            init {
                if (multiple) {
                    isMultipleMode = true
                }
            }

            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (multiple) {
                        onCloseRequest(files?.map { Path(it.absolutePath) } ?: emptyList())
                    } else {
                        if (directory != null && file != null) {
                            onCloseRequest(listOf(Path(directory + file)))
                        } else {
                            onCloseRequest(emptyList())
                        }
                    }
                }
            }
        }
    },
    dispose = FileDialog::dispose,
)
