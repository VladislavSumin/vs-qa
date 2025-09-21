package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.Pages
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import kotlin.io.path.name

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

    private val tabs = childNavigationPages(
        navigationHost = TabNavigationHost,
        initialPages = { Pages() },
    )

    @Composable
    override fun Render(modifier: Modifier) {
        Column(modifier) {
            Row {
                val pages = tabs.subscribeAsState().value
                if (pages.items.size > 1) {
                    pages.items.forEach {
                        Text(
                            text = (it.configuration.screenParams as LogViewerScreenParams).logPath.name,
                            Modifier.clickable(onClick = { navigator.open(it.configuration.screenParams) }),
                        )
                        QaIconButton(onClick = { navigator.close(it.configuration.screenParams) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                        }
                    }
                }
            }
            ChildPages(
                tabs,
                onPageSelected = { _ -> },
                Modifier.weight(1f),
            ) { _, page ->
                // TODO разобраться почему это работает?
                val hackyContent = remember(page) {
                    movableContentOf {
                        page.Render(Modifier)
                    }
                }
                hackyContent()
            }
            bottomBarComponent.Render(Modifier)
        }
    }
}
