package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.navigator.ScreenNavigator
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
    navigator: ScreenNavigator<*>, // TODO убрать навигатор отсюда.
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    if (state) FilePickerDialog(viewModel::onOpenNewFileDialogResult)

    Box(modifier) {
        val pages by tabs.subscribeAsState()

        if (pages.items.isEmpty()) EmptyTabsPlaceholder()

        Column(modifier) {
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

@Composable
private fun EmptyTabsPlaceholder() {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "All tabs closed\nPress Command + O for open new one",
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
        )
    }
}
