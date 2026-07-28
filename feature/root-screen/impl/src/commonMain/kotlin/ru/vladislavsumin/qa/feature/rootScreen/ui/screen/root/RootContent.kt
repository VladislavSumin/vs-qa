package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.debug.FrameMeasureOverlay
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

@Composable
internal fun RootContent(
    tabs: Value<ChildPages<ConfigurationHolder, Screen>>,
    tabsComponent: ComposeComponent,
    bottomBarComponent: ComposeComponent,
    notificationsComponent: ComposeComponent,
    modifier: Modifier,
) {
    FrameMeasureOverlay(
        modifier,
        // TODO добавить автоматику
        logSlowFrames = false,
        flashOnSlowFrame = false,
    ) {
        Surface(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            color = QaTheme.colorScheme.background1,
            contentColor = QaTheme.colorScheme.content1,
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
}

@Composable
private fun ColumnScope.TabsContent(tabs: Value<ChildPages<ConfigurationHolder, Screen>>) {
    val tabsState by tabs.subscribeAsState()
    val expectedPage = tabsState.selectedIndex.coerceAtLeast(0)

    val isTabExists = tabsState.items.size > 1
    val isFirst = tabsState.selectedIndex == 0

    val shape = if (isTabExists) {
        if (isFirst) {
            QaTheme.shapes.small.copy(topStart = CornerSize(0.dp))
        } else {
            QaTheme.shapes.small
        }
    } else {
        RectangleShape
    }

    ChildPages(
        pages = tabs,
        pager = { modifier, state, key, pageContent ->
            NonScrollablePager(modifier, state, key, pageContent, expectedPage)
        },
        onPageSelected = { _ -> },
        modifier = Modifier
            .weight(1f)
            .background(QaTheme.colorScheme.background2, shape),
    ) { _, page ->
        val hackyContent = remember(page) {
            movableContentOf {
                page.Render(Modifier)
            }
        }
        hackyContent()
    }
}
