package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.charleskorn.kaml.Yaml
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyDispatcher
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor

@Composable
internal expect fun WindowContent(
    screen: Value<ChildSlot<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    yaml: Yaml,
    windowTitleInteractor: WindowTitleInteractor?,
    globalHotkeyDispatcher: GlobalHotkeyDispatcher,
    lifecycleRegistry: LifecycleRegistry,
    onCloseRequest: () -> Unit,
    modifier: Modifier,
)
