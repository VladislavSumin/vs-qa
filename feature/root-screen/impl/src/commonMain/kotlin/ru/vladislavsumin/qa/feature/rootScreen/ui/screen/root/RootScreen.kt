package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.essenty.lifecycle.Lifecycle
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import ru.vladislavsumin.qa.feature.debug.ui.screen.debug.DebugScreenParams
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenFactory
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenParams
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsComponentFactory
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabsComponentFactory
import kotlin.random.Random

@GenerateFactory(RootScreenFactory::class)
internal class RootScreen(
    bottomBarComponentFactory: BottomBarComponentFactory,
    logViewerScreenFactory: LogViewerScreenFactory,
    homeScreenFactory: HomeScreenFactory,
    adbDeviceScreenFactory: AdbDeviceScreenFactory,
    notificationsComponentFactory: NotificationsComponentFactory,
    tabsComponentFactory: TabsComponentFactory,
    windowTitleInteractor: WindowTitleInteractor?,
    globalHotkeyManager: GlobalHotkeyManager,
    context: ComponentContext,
) : Screen(context) {

    private val bottomBarComponent = bottomBarComponentFactory.create(context.childContext("bottom-bar"))
    private val notificationsComponent = notificationsComponentFactory.create(context.childContext("notifications"))

    init {
        registerCustomFactory { context, params, intents ->
            logViewerScreenFactory.create(
                bottomBarUiInteractor = bottomBarComponent.bottomBarUiInteractor,
                notificationsUiInteractor = notificationsComponent.notificationsUiInteractor,
                globalHotkeyManager = globalHotkeyManager,
                params = params,
                intents = intents,
                context = context,
            )
        }
        registerCustomFactory { context, params, _ ->
            homeScreenFactory.create(
                notificationsUiInteractor = notificationsComponent.notificationsUiInteractor,
                bottomBarUiInteractor = bottomBarComponent.bottomBarUiInteractor,
                globalHotkeyManager = globalHotkeyManager,
                params = params,
                context = context,
            )
        }
        registerCustomFactory { context, params, _ ->
            adbDeviceScreenFactory.create(
                params = params,
                context = context,
            )
        }
    }

    private val tabsResult = childNavigationPages(
        navigationHost = TabNavigationHost,
        // TODO возможно для андроида нужно другое поведение?
        // Не уничтожаем контент табов.
        // Это нужно по двум причинам:
        // 1) Перезагрузка табов может требовать длительного времени восстановления данных в них
        // 2) Механизм tabsComponent требует что бы все табы были активны.
        pageStatus = { index, pages ->
            if (index == pages.selectedIndex) {
                ChildNavState.Status.RESUMED
            } else {
                ChildNavState.Status.CREATED
            }
        },
        initialPages = { Pages(items = listOf(DebugScreenParams, HomeScreenParams), selectedIndex = 1) },
        closeParentWhenEmpty = true,
    )
    private val tabs = tabsResult.pages
    private val tabsController = tabsResult.controller

    private val tabsComponent = tabsComponentFactory.create(
        windowTitleInteractor = windowTitleInteractor,
        pages = tabs,
        onTabClick = { navigator.open(it) },
        onTabClickClose = { navigator.close(it) },
        onTabClickDetach = { navigator.transfer(it, hints = listOf(WindowScreenParams(Random.nextLong().toString()))) },
        onTabReorder = { from, to -> tabsController.reorder(from, to) },
        context = context.childContext("tabs"),
    )

    init {
        @Suppress("MagicNumber")
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            globalHotkeyManager.subscribe(
                KeyModifier.Command + Key.N to { focusTab(0) },
                KeyModifier.Command + Key.One to { focusTab(1) },
                KeyModifier.Command + Key.Two to { focusTab(2) },
                KeyModifier.Command + Key.Three to { focusTab(3) },
                KeyModifier.Command + Key.Four to { focusTab(4) },
                KeyModifier.Command + Key.Five to { focusTab(5) },
                KeyModifier.Command + Key.Six to { focusTab(6) },
                KeyModifier.Command + Key.Seven to { focusTab(7) },
                KeyModifier.Command + Key.Eight to { focusTab(8) },
                KeyModifier.Command + Key.Nine to { focusTab(9) },
                KeyModifier.Command + Key.Zero to { focusTab(10) },
            )
        }
    }

    private fun focusTab(number: Int): Boolean {
        tabs.value.items.getOrNull(number)
            ?.let { navigator.open(it.configuration.screenParams) }
        return true
    }

    @Composable
    override fun Render(modifier: Modifier) = RootContent(
        tabs,
        tabsComponent,
        bottomBarComponent,
        notificationsComponent,
        modifier,
    )
}
