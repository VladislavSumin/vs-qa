package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.charleskorn.kaml.Yaml
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.host.childNavigationSlot
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyDispatcher
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractorImpl
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenFactory
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenParams

@GenerateScreenFactory
internal class WindowScreen(
    private val yaml: Yaml,
    private val rootScreenFactory: RootScreenFactory,
    context: ComponentContext,
) : Screen(context) {

    private val windowTitleInteractor: WindowTitleInteractor = WindowTitleInteractorImpl()
    private val globalHotkeyDispatcher = GlobalHotkeyDispatcher()

    init {
        registerCustomFactory { context, _: RootScreenParams, _ ->
            rootScreenFactory.create(windowTitleInteractor, globalHotkeyDispatcher, context)
        }
    }

    private val lifecycleRegistry = LifecycleRegistry()

    private val screen: Value<ChildSlot<ConfigurationHolder, GenericScreen<ComponentContext>>> = childNavigationSlot(
        navigationHost = WindowNavigationHost,
        extraLifecycle = lifecycleRegistry,
        initialConfiguration = { RootScreenParams },
    )

    @Composable
    override fun Render(modifier: Modifier) = WindowContent(
        screen = screen,
        yaml = yaml,
        windowTitleInteractor = windowTitleInteractor,
        globalHotkeyDispatcher = globalHotkeyDispatcher,
        lifecycleRegistry = lifecycleRegistry,
        onCloseRequest = { navigator.close() },
        modifier = modifier,
    )
}
