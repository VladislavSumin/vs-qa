package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import java.nio.file.Path

@GenerateFactory
@Stable
internal class HomeScreenViewModel : NavigationViewModel() {
    private val showOpenNewFileDialog = MutableStateFlow(false)
    val state: StateFlow<Boolean> = showOpenNewFileDialog

    fun onClickOpen() {
        showOpenNewFileDialog.value = true
    }

    fun onOpenNewFileDialogResult(path: Path?) {
        showOpenNewFileDialog.value = false
        if (path != null) {
            open(LogViewerScreenParams(path))
        }
    }

    fun onDragAndDropLogsFiles(paths: List<Path>) {
        paths.forEach { open(LogViewerScreenParams(it)) }
    }
}
