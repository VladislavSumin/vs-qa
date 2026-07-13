package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.flow.combine
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyDispatcher
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.qa.feature.multiWindow.ui.locale.AppEnvironment
import ru.vladislavsumin.qa.feature.multiWindow.ui.locale.toLocaleTag
import ru.vladislavsumin.qa.feature.settings.domain.AppLanguage
import ru.vladislavsumin.qa.feature.settings.domain.SettingsInteractor

@Composable
internal actual fun WindowContent(
    screen: Value<ChildSlot<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    yaml: Yaml,
    settingsInteractor: SettingsInteractor,
    windowTitleInteractor: WindowTitleInteractor?,
    globalHotkeyDispatcher: GlobalHotkeyDispatcher,
    lifecycleRegistry: LifecycleRegistry,
    onCloseRequest: () -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier,
) {
    val windowState = rememberWindowState()
    val windowTitleExtension by windowTitleInteractor!!.windowTitleExtension.collectAsState()
    val language by settingsInteractor.language.collectAsState(AppLanguage.SYSTEM)
    val title = "vs-qa"
    val windowTitle = if (windowTitleExtension == null) title else "$title: $windowTitleExtension"

    Window(
        title = windowTitle,
        onCloseRequest = onCloseRequest,
        state = windowState,
        onKeyEvent = globalHotkeyDispatcher::onKeyEvent,
    ) {
        val windowInfo = LocalWindowInfo.current
        val isFocused = windowInfo.isWindowFocused

        LaunchedEffect(isFocused) {
            if (isFocused) {
                onFocused()
            }
        }

        LifecycleController(lifecycleRegistry, windowState, windowInfo)
        // TODO вынести тему отдельно
        AppEnvironment(language.toLocaleTag()) {
            QaTheme(yaml) {
                Surface {
                    screen.value.child?.instance?.Render(modifier)
                }
            }
        }
    }
}

/**
 * Копия LifecycleController Аркадия в которой убрал нижний DisposableEffect что бы избежать краша
 */
@Composable
fun LifecycleController(
    lifecycleRegistry: LifecycleRegistry,
    windowState: WindowState,
    windowInfo: WindowInfo? = null,
) {
    LaunchedEffect(lifecycleRegistry, windowState, windowInfo) {
        combine(
            snapshotFlow(windowState::isMinimized),
            snapshotFlow { windowInfo?.isWindowFocused ?: true },
            ::Pair,
        ).collect { (isMinimized, isFocused) ->
            when {
                isMinimized -> lifecycleRegistry.stop()
                isFocused -> lifecycleRegistry.resume()
                lifecycleRegistry.state == Lifecycle.State.RESUMED -> lifecycleRegistry.pause()
                else -> lifecycleRegistry.start()
            }
        }
    }

//    DisposableEffect(lifecycleRegistry) {
//        lifecycleRegistry.create()
//        onDispose(lifecycleRegistry::destroy)
//    }
}
