package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.childNavigationSlot
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory

@GenerateScreenFactory
internal class RootScreen(
    bottomBarComponentFactory: BottomBarComponentFactory,
    logViewerScreenFactory: LogViewerScreenFactory,
    context: ComponentContext,
) : Screen(context) {

    private val bottomBarComponent = bottomBarComponentFactory.create(context.childContext("bottom-bar"))

    init {
        registerCustomFactory { context, params, _ ->
            logViewerScreenFactory.create(
                bottomBarUiInteractor = bottomBarComponent.bottomBarUiInteractor,
                params = params,
                context = context,
            )
        }
    }

    private val tabs = childNavigationSlot(TabNavigationHost)

    @Composable
    override fun Render(modifier: Modifier) {
        Column(modifier) {
            val child = tabs.subscribeAsState().value.child
            // TODO разобраться почему это работает?
            val hackyContent = remember(child) {
                movableContentOf {
                    child?.instance?.Render(Modifier.weight(1f))
                }
            }
            hackyContent()
            bottomBarComponent.Render(Modifier)
        }
    }
}
