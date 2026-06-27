package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen

@Composable
internal fun RootContent(
    tabs: Value<ChildPages<ConfigurationHolder, Screen>>,
    tabsComponent: ComposeComponent,
    bottomBarComponent: ComposeComponent,
    notificationsComponent: ComposeComponent,
    modifier: Modifier,
) {
    Box(
        modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Column(modifier) {
            Box(Modifier.weight(1f)) {
                Column {
                    tabsComponent.Render(Modifier.fillMaxWidth())
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
