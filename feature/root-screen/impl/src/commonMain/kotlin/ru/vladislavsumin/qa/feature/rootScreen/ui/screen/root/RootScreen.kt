package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.Pages
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import kotlin.io.path.name

@GenerateScreenFactory
internal class RootScreen(
    viewModelFactory: RootViewModelFactory,
    bottomBarComponentFactory: BottomBarComponentFactory,
    logViewerScreenFactory: LogViewerScreenFactory,
    context: ComponentContext,
) : Screen(context) {

    private val viewModel = viewModel { viewModelFactory.create() }
    private val bottomBarComponent = bottomBarComponentFactory.create(context.childContext("bottom-bar"))

    init {
        registerCustomFactory { context, params, intents ->
            logViewerScreenFactory.create(
                bottomBarUiInteractor = bottomBarComponent.bottomBarUiInteractor,
                params = params,
                intents = intents,
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
        val state by viewModel.state.collectAsState()
        if (state) FilePickerDialog(viewModel::onOpenNewFileDialogResult)

        Column(modifier) {
            val pages = tabs.subscribeAsState().value
            if (pages.items.size > 1) {
                LazyRow {
                    itemsIndexed(pages.items, key = { _, item -> item.configuration.screenParams }) { index, item ->
                        Row(
                            modifier = Modifier
                                .background(
                                    if (index == pages.selectedIndex) {
                                        QaTheme.colorScheme.surfaceVariant
                                    } else {
                                        QaTheme.colorScheme.surface
                                    },
                                )
                                .clickable(onClick = { navigator.open(item.configuration.screenParams) }),
                            verticalAlignment = Alignment.CenterVertically,

                        ) {
                            Text(
                                text = (item.configuration.screenParams as LogViewerScreenParams).logPath.name,
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                            )
                            QaIconButton(
                                onClick = { navigator.close(item.configuration.screenParams) },
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                            }
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
