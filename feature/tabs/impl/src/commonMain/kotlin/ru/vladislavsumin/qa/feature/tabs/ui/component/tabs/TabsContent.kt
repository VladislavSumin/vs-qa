package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hint.hint

@Composable
internal fun TabsContent(
    pages: Value<ChildPages<ConfigurationHolder, Screen>>,
    onTabClick: (IntentScreenParams<*>) -> Unit,
    onTabClickClose: (IntentScreenParams<*>) -> Unit,
    modifier: Modifier,
) {
    val pages by pages.subscribeAsState()
    if (pages.items.size > 1) {
        LazyRow(modifier) {
            itemsIndexed(
                pages.items,
                key = { _, item ->
                    // TODO это кривой фикс краша на андроид, там только бандлы сохраняются.
                    // Нужно придумать что то адекватное
                    item.configuration.screenParams.toString()
                },
            ) { index, item ->
                Tab(index, pages, item, onTabClick, onTabClickClose)
            }
        }
    }
}

@Composable
private fun Tab(
    index: Int,
    pages: ChildPages<ConfigurationHolder, Screen>,
    item: Child<ConfigurationHolder, Screen>,
    onTabClick: (IntentScreenParams<*>) -> Unit,
    onTabClickClose: (IntentScreenParams<*>) -> Unit,
) {
    val provider = (item.instance as? TabSupport)
    val state = provider?.tabState?.collectAsState()?.value ?: UNKNOWN_TAB

    val colorScheme = QaTheme.colorScheme
    val background = if (index == pages.selectedIndex) colorScheme.surfaceVariant else colorScheme.surface

    Row(
        modifier = Modifier
            .background(background)
            .clickable(onClick = { onTabClick((item.configuration.screenParams)) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = state.icon
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = "home")
        }
        val text = state.name
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            )
        }
        if (state.allowClose) {
            QaIconButton(
                onClick = { onTabClickClose(item.configuration.screenParams) },
                modifier = Modifier.hint("Close tab").padding(end = 4.dp),
            ) { Icon(imageVector = Icons.Default.Close, contentDescription = "close") }
        }
    }
}

private val UNKNOWN_TAB = TabSupport.TabState(
    name = "<UNKNOWN_TAB>",
)
