package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.charleskorn.kaml.Yaml
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor

@Composable
internal actual fun WindowContent(
    screen: Value<ChildSlot<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    yaml: Yaml,
    windowTitleInteractor: WindowTitleInteractor?,
    onCloseRequest: () -> Unit,
    modifier: Modifier,
) {
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    val windowTitleExtension by windowTitleInteractor!!.windowTitleExtension.collectAsState()
    val title = "vs-qa"
    val windowTitle = if (windowTitleExtension == null) title else "$title: $windowTitleExtension"

    // TODO разобраться с lifecycle
    // LifecycleController(lifecycle, windowState)

    Window(
        title = windowTitle,
        onCloseRequest = onCloseRequest,
        state = windowState,
        // TODO переделать hotkeyDispatcher
//            onKeyEvent = hotkeyDispatcher::onKeyEvent,
    ) {
        // TODO вынести тему отдельно
        QaTheme(yaml) {
            Surface {
                screen.value.child?.instance?.Render(modifier)
            }
        }
    }
}
