package ru.vladislavsumin.core.ui.filePicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Path
import kotlin.io.path.Path

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilePickerDialog(onCloseRequest: (result: Path?) -> Unit) = AwtWindow(
    create = {
        object : FileDialog(null as Frame?, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (directory != null && file != null) {
                        onCloseRequest(Path(directory + file))
                    } else {
                        onCloseRequest(null)
                    }
                }
            }
        }
    },
    dispose = FileDialog::dispose,
)
