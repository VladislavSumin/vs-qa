package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.charleskorn.kaml.Yaml
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyDispatcher
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor

@Composable
internal actual fun WindowContent(
    screen: Value<ChildSlot<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    yaml: Yaml,
    windowTitleInteractor: WindowTitleInteractor?,
    @Suppress("UnusedParameter") globalHotkeyDispatcher: GlobalHotkeyDispatcher,
    lifecycleRegistry: LifecycleRegistry,
    onCloseRequest: () -> Unit,
    @Suppress("UnusedParameter") onFocused: () -> Unit,
    modifier: Modifier,
) {
    LaunchedEffect(lifecycleRegistry) {
        // Тут все управляется внешним lifecycle так что внутренним управлять явно не нужно
        lifecycleRegistry.resume()
    }
    // TODO вынести тему отдельно
    QaTheme(yaml) {
        Surface(Modifier.onKeyEvent(globalHotkeyDispatcher::onKeyEvent)) {
            screen.value.child?.instance?.Render(modifier)
        }
    }
}
