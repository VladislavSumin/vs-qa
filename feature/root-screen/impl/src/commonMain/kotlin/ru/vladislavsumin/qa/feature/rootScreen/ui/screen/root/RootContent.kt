package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams
import kotlin.io.path.name

@Composable
internal fun RootContent(
    viewModel: RootViewModel,
    tabs: Value<ChildPages<ConfigurationHolder, Screen>>,
    bottomBarComponent: ComposeComponent,
    notificationsComponent: ComposeComponent,
    modifier: Modifier,
) {
    Box(
        modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        val pages by tabs.subscribeAsState()
        Column(modifier) {
            Box(Modifier.weight(1f)) {
                Column {
                    Tabs(viewModel, pages)
                    TabsContent(tabs)
                }
                notificationsComponent.Render(
                    Modifier
                        .padding(bottom = 48.dp, end = 48.dp)
                        .align(Alignment.BottomEnd),
                )
            }
            bottomBarComponent.Render(Modifier)
        }
    }
}

@Composable
private fun Tabs(viewModel: RootViewModel, pages: ChildPages<ConfigurationHolder, Screen>) {
    if (pages.items.size > 1) {
        LazyRow {
            itemsIndexed(
                pages.items,
                key = { _, item ->
                    // TODO это кривой фикс краша на андроид, там только бандлы сохраняются.
                    // Нужно придумать что то адекватное
                    item.configuration.screenParams.toString()
                },
            ) { index, item ->
                Tab(viewModel, index, pages, item)
            }
        }
    }
}

@Composable
private fun Tab(
    viewModel: RootViewModel,
    index: Int,
    pages: ChildPages<ConfigurationHolder, Screen>,
    item: Child<ConfigurationHolder, Screen>,
) {
    val colorScheme = QaTheme.colorScheme
    val background = if (index == pages.selectedIndex) colorScheme.surfaceVariant else colorScheme.surface

    when (val screenParams = item.configuration.screenParams) {
        is LogViewerScreenParams -> {
            Row(
                modifier = Modifier
                    .background(background)
                    .clickable(onClick = { viewModel.onTabClick(item.configuration.screenParams) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = screenParams.logPath.name,
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                )
                QaIconButton(
                    onClick = { viewModel.onCloseTabClick(item.configuration.screenParams) },
                    modifier = Modifier.padding(end = 4.dp),
                ) { Icon(imageVector = Icons.Default.Close, contentDescription = "close") }
            }
        }

        is HomeScreenParams -> {
            QaIconButton(
                onClick = { viewModel.onClickHome() },
                modifier = Modifier
                    .background(background)
                    .padding(end = 4.dp),
            ) { Icon(imageVector = Icons.Default.Home, contentDescription = "home") }
        }

        else -> error("Unexpected screenParams = $screenParams")
    }
}

@Composable
private fun ColumnScope.TabsContent(tabs: Value<ChildPages<ConfigurationHolder, Screen>>) {
    ChildPages(
        pages = tabs,
        pager = { modifier, state, key, pageContent ->
            HorizontalPager(
                modifier = modifier,
                state = state,
                key = key,
                userScrollEnabled = false,
                pageContent = pageContent,
            )
        },
        onPageSelected = { _ -> },
        modifier = Modifier.weight(1f),
    ) { _, page ->
        // TODO разобраться почему это работает?
        val hackyContent = remember(page) {
            movableContentOf {
                page.Render(Modifier)
            }
        }
        hackyContent()
    }
}
