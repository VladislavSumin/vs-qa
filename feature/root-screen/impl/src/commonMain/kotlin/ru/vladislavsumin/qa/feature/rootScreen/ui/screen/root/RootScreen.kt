package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.pages.Pages
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsComponentFactory

@GenerateScreenFactory
internal class RootScreen(
    viewModelFactory: RootViewModelFactory,
    bottomBarComponentFactory: BottomBarComponentFactory,
    logViewerScreenFactory: LogViewerScreenFactory,
    notificationsComponentFactory: NotificationsComponentFactory,
    context: ComponentContext,
) : Screen(context) {

    private val viewModel = viewModel { viewModelFactory.create() }
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
    }

    private val tabs = childNavigationPages(
        navigationHost = TabNavigationHost,
        // TODO https://github.com/arkivanov/Decompose/issues/866 исправлено в 3.4.0-alpha2 жду стабильную версию.
        initialPages = { Pages(items = emptyList(), selectedIndex = 0) },
    )

    init {
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
    override fun Render(modifier: Modifier) =
        RootContent(viewModel, tabs, bottomBarComponent, notificationsComponent, navigator, modifier)
}
