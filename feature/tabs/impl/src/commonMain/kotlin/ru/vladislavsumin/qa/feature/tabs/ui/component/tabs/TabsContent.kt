package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.core.ui.icons.QaIcons
import ru.vladislavsumin.feature.tabs.impl.generated.resources.Res
import ru.vladislavsumin.feature.tabs.impl.generated.resources.tabs_close
import ru.vladislavsumin.feature.tabs.impl.generated.resources.tabs_close_hint
import ru.vladislavsumin.feature.tabs.impl.generated.resources.tabs_detach
import ru.vladislavsumin.feature.tabs.impl.generated.resources.tabs_detach_hint
import ru.vladislavsumin.feature.tabs.impl.generated.resources.tabs_icon
import ru.vladislavsumin.qa.feature.multiWindow.isMultiWindowSupported
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun TabsContent(
    pages: Value<ChildPages<ConfigurationHolder, Screen>>,
    onTabClick: (IntentScreenParams<*>) -> Unit,
    onTabClickClose: (IntentScreenParams<*>) -> Unit,
    onTabClickDetach: (IntentScreenParams<*>) -> Unit,
    onTabReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier,
) {
    val pages by pages.subscribeAsState()
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onTabReorder(from.index, to.index)
    }

    if (pages.items.size > 1) {
        LazyRow(modifier, state = lazyListState) {
            itemsIndexed(
                pages.items,
                key = { _, item ->
                    // TODO это кривой фикс краша на андроид, там только бандлы сохраняются.
                    // Нужно придумать что то адекватное
                    item.configuration.screenParams.toString()
                },
            ) { index, item ->
                ReorderableItem(
                    reorderableState,
                    key = item.configuration.screenParams.toString(),
                    modifier = Modifier.zIndex(if (index == pages.selectedIndex) 1f else 0f),
                    animateItemModifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                ) {
                    Tab(
                        index = index,
                        pages = pages,
                        item = item,
                        onTabClick = onTabClick,
                        onTabClickClose = onTabClickClose,
                        onTabClickDetach = onTabClickDetach,
                        modifier = if (useLongPressForDrag()) {
                            Modifier.longPressDraggableHandle()
                        } else {
                            Modifier.draggableHandle()
                        },
                    )
                }
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
    onTabClickDetach: (IntentScreenParams<*>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val provider = (item.instance as? TabSupport)
    val state = provider?.tabState?.collectAsState()?.value ?: UNKNOWN_TAB
    val isSelected = index == pages.selectedIndex
    val colorScheme = QaTheme.colorScheme

    val background = if (isSelected) colorScheme.background2 else colorScheme.background1
    val shape = if (isSelected) TabShape() else RectangleShape

    Row(
        modifier = modifier
            .background(background, shape = shape)
            .padding(2.dp)
            .clip(QaTheme.shapes.extraSmall)
            .clickable(onClick = { onTabClick((item.configuration.screenParams)) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = state.icon
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = stringResource(Res.string.tabs_icon))
        }
        val text = state.nameRes?.let { stringResource(it) } ?: state.name
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            )
        }
        if (state.allowDetach && isMultiWindowSupported()) {
            QaIconButton(
                onClick = { onTabClickDetach(item.configuration.screenParams) },
                modifier = Modifier.hint(stringResource(Res.string.tabs_detach_hint)),
            ) {
                Icon(
                    imageVector = QaIcons.OpenInNew,
                    contentDescription = stringResource(Res.string.tabs_detach),
                )
            }
        }
        if (state.allowClose) {
            QaIconButton(
                onClick = { onTabClickClose(item.configuration.screenParams) },
                modifier = Modifier.hint(stringResource(Res.string.tabs_close_hint)).padding(end = 4.dp),
            ) { Icon(imageVector = QaIcons.Close, contentDescription = stringResource(Res.string.tabs_close)) }
        }
    }
}

private val UNKNOWN_TAB = TabSupport.TabState(
    name = "<UNKNOWN_TAB>",
)
