package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.Pages
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import ru.vladislavsumin.qa.feature.debug.ui.screen.debug.DebugScreenParams
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenFactory
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsComponentFactory
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabsComponentFactory

@GenerateFactory(RootScreenFactory::class)
internal class RootScreen(
    viewModelFactory: RootViewModelFactory,
    bottomBarComponentFactory: BottomBarComponentFactory,
    logViewerScreenFactory: LogViewerScreenFactory,
    homeScreenFactory: HomeScreenFactory,
    adbDeviceScreenFactory: AdbDeviceScreenFactory,
    notificationsComponentFactory: NotificationsComponentFactory,
    tabsComponentFactory: TabsComponentFactory,
    windowTitleInteractor: WindowTitleInteractor?,
    context: ComponentContext,
) : Screen(context) {

    private val viewModel: RootViewModel = viewModel { viewModelFactory.create() }
    private val bottomBarComponent = bottomBarComponentFactory.create(context.childContext("bottom-bar"))
    private val notificationsComponent = notificationsComponentFactory.create(context.childContext("notifications"))

    init {
        registerCustomFactory { context, params, intents ->
            logViewerScreenFactory.create(
                bottomBarUiInteractor = bottomBarComponent.bottomBarUiInteractor,
                notificationsUiInteractor = notificationsComponent.notificationsUiInteractor,
                params = params,
                intents = intents,
                context = context,
            )
        }
        registerCustomFactory { context, params, _ ->
            homeScreenFactory.create(
                notificationsUiInteractor = notificationsComponent.notificationsUiInteractor,
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

    private val tabs = childNavigationPages(
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
    )

    private val tabsComponent = tabsComponentFactory.create(
        windowTitleInteractor = windowTitleInteractor,
        pages = tabs,
        onTabClick = { navigator.open(it) },
        onTabClickClose = { navigator.close(it) },
        context = context.childContext("tabs"),
    )

    init {
        viewModel // touch for init
        launch {
            for (event in viewModel.events) {
                when (event) {
                    is RootEvent.FocusTab -> {
                        tabs.value.items.getOrNull(event.number)
                            ?.let { navigator.open(it.configuration.screenParams) }
                    }
                }
            }
        }
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
