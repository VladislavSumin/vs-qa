package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val language by settingsInteractor.language.collectAsState(AppLanguage.SYSTEM)
    val isLiquidGlass by settingsInteractor.isLiquidGlass.collectAsState(false)

    // TODO вынести тему отдельно
    AppEnvironment(language.toLocaleTag()) {
        QaTheme(yaml, isLiquidGlass) {
            screen.value.child?.instance?.Render(
                modifier = modifier.onKeyEvent(globalHotkeyDispatcher::onKeyEvent),
            )
        }
    }
}
