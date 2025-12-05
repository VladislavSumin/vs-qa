package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import kotlin.io.path.name

@Composable
internal fun RootContent(
    viewModel: RootViewModel,
    tabs: Value<ChildPages<ConfigurationHolder, Screen>>,
    bottomBarComponent: ComposeComponent,
    notificationsComponent: ComposeComponent,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    if (state) FilePickerDialog(mimeType = "application/zip", onCloseRequest = viewModel::onOpenNewFileDialogResult)

    Box(modifier) {
        val pages by tabs.subscribeAsState()

        if (pages.items.isEmpty()) EmptyTabsPlaceholder(viewModel)

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
            itemsIndexed(pages.items, key = { _, item -> item.configuration.screenParams }) { index, item ->
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

    Row(
        modifier = Modifier
            .background(background)
            .clickable(onClick = { viewModel.onTabClick(item.configuration.screenParams) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = (item.configuration.screenParams as LogViewerScreenParams).logPath.name,
            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
        )
        QaIconButton(
            onClick = { viewModel.onCloseTabClick(item.configuration.screenParams) },
            modifier = Modifier.padding(end = 4.dp),
        ) { Icon(imageVector = Icons.Default.Close, contentDescription = "close") }
    }
}

@Composable
private fun ColumnScope.TabsContent(tabs: Value<ChildPages<ConfigurationHolder, Screen>>) {
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
}

@Composable
private fun EmptyTabsPlaceholder(viewModel: RootViewModel) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "All tabs closed\nPress Command + O for open new one",
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::onClickOpen) { Text("Open new file") }
        }
    }
}
