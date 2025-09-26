package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.ui.input.key.Key
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import java.nio.file.Path

@GenerateFactory
internal class RootViewModel(
    globalHotkeyManager: GlobalHotkeyManager,
) : NavigationViewModel() {
    private val showOpenNewFileDialog = MutableStateFlow(false)
    val state: StateFlow<Boolean> = showOpenNewFileDialog

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            globalHotkeyManager.subscribe(
                KeyModifier.Command + Key.O to { !showOpenNewFileDialog.getAndUpdate { true } },
            )
        }
    }

    fun onOpenNewFileDialogResult(path: Path?) {
        showOpenNewFileDialog.value = false
        if (path != null) {
            open(LogViewerScreenParams(path))
        }
    }
}
