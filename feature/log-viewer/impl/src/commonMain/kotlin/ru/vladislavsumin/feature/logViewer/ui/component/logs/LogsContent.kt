package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.domain.logs.LogOrder
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.TextSelectionSeparator
import ru.vladislavsumin.feature.logViewer.ui.utils.VsVerticalScrollbar
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
@Suppress("LongMethod")
internal fun LogsContent(
    onFirstVisibleIndexChange: (Int) -> Unit,
    events: ReceiveChannel<LogsEvents>,
    state: StateFlow<LogsViewState>,
    onAddTimeFilter: (LogOrder, Boolean) -> Unit,
    modifier: Modifier,
) {
    val state by state.collectAsState()
    val logs = state.logs
    val maxLogNumberDigits = state.maxLogNumberDigits
    val textSizeDp = measureTextWidth(
        " ".repeat(maxLogNumberDigits),
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = state.logFontSize.sp,
        ),
    )
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect(onFirstVisibleIndexChange)
    }

    val density = LocalDensity.current
    LaunchedEffect(lazyListState) {
        events.receiveAsFlow().collect { event ->
            when (event) {
                is LogsEvents.ScrollToIndex -> lazyListState.scrollToItem(
                    event.index,
                    // -24 это высота заголовка. ну если его нет будет небольшой сдвиг в целом ок.
                    scrollOffset = with(density) { -24.dp.roundToPx() },
                )
            }
        }
    }

    var contextMenuTarget by remember { mutableStateOf<LogOrder?>(null) }
    var contextMenuPosition by remember { mutableStateOf<Offset?>(null) }

    Row(modifier) {
        Box(Modifier.weight(1f)) {
            Scaffold(
                containerColor = Color.Unspecified,
                floatingActionButton = { ScrollToBottom(lazyListState = lazyListState) },
            ) { innerPadding ->
                SelectionContainer {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                    ) {
                        logs.forEachIndexed { runNumber, sectionInfo ->
                            if (state.showRunNumbers) {
                                stickyHeader(key = -runNumber - 1) {
                                    Header(
                                        runNumber + 1,
                                        sectionInfo.meta,
                                        state.logFontSize + 2,
                                        textSizeDp,
                                    )
                                }
                            }
                            items(sectionInfo.logs, { it.order.value }) {
                                Record(
                                    log = it,
                                    isSelected = it.order == state.currentSelectedItemOrder,
                                    stripDate = state.stripDate,
                                    fontSize = state.logFontSize,
                                    textSizeDp = textSizeDp,
                                    showContextMenu = contextMenuTarget == it.order,
                                    contextMenuPosition = if (contextMenuTarget == it.order) {
                                        contextMenuPosition
                                    } else {
                                        null
                                    },
                                    onShowContextMenu = { offset ->
                                        contextMenuTarget = it.order
                                        contextMenuPosition = offset
                                    },
                                    onDismissContextMenu = {
                                        contextMenuTarget = null
                                        contextMenuPosition = null
                                    },
                                    onAddTimeFilter = onAddTimeFilter,
                                )
                            }
                        }
                    }
                }
            }
            VerticalDivider(Modifier.padding(start = textSizeDp + 8.dp))
        }
        LogsVerticalScrollBar(lazyListState)
    }
}

@Composable
private fun ScrollToBottom(lazyListState: LazyListState) {
    // TODO сделать нормальные расширения для адаптивной верстки
    val withDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    if (withDp <= 600.dp) {
        val scope = rememberCoroutineScope()
        AnimatedVisibility(
            visible = lazyListState.isScrollingUp().value,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = scaleOut(targetScale = 0.7f) + fadeOut(),
        ) {
            FloatingActionButton(
                onClick = { scope.launch { lazyListState.scrollToItem(Int.MAX_VALUE, 0) } },
            ) {
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
    }
}

@Composable
private fun Header(runNumber: Int, meta: Map<String, String>?, fontSize: Int, textSizeDp: Dp) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(QaTheme.colorScheme.surfaceVariant),
    ) {
        val text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Run $runNumber  ")
            }
            meta?.forEach { (k, v) ->
                withStyle(SpanStyle(color = QaTheme.colorScheme.logTrace.primary)) {
                    append(k)
                    append("=")
                }
                append(v)
                append(" ")
            }
        }
        Text(
            text = text,
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp * 1.42,
            modifier = Modifier.padding(start = textSizeDp + 13.dp + 8.dp, end = 4.dp),
        )
    }
}

@Composable
private fun Record(
    log: LogRecord,
    isSelected: Boolean,
    stripDate: Boolean,
    fontSize: Int,
    textSizeDp: Dp,
    showContextMenu: Boolean,
    contextMenuPosition: Offset?,
    onShowContextMenu: (Offset) -> Unit,
    onDismissContextMenu: () -> Unit,
    onAddTimeFilter: (LogOrder, Boolean) -> Unit,
) {
    Box(
        modifier = Modifier.onRightClick(onShowContextMenu),
    ) {
        DisableSelection {
            Text(
                // order нумеруется с 0, но визуально записи более правильно нумеровать с единицы.
                text = "${log.order.value + 1}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = fontSize.sp,
                // TODO вынести в константу что ли?
                lineHeight = fontSize.sp * 1.42,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .defaultMinSize(minWidth = textSizeDp),
            )
        }
        Text(
            text = log.colorize(isSelected, stripDate),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp * 1.42,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth() // Что бы выделение работало после конца текста
                .padding(start = textSizeDp + 13.dp),
        )
        TextSelectionSeparator()

        LogItemDropDownMenu(showContextMenu, contextMenuPosition, onDismissContextMenu, log, onAddTimeFilter)
    }
}

@Composable
private fun LogItemDropDownMenu(
    showContextMenu: Boolean,
    position: Offset?,
    onDismissContextMenu: () -> Unit,
    log: LogRecord,
    onAddTimeFilter: (LogOrder, Boolean) -> Unit,
) {
    if (showContextMenu && position != null) {
        DisableSelection {
            Popup(
                popupPositionProvider = remember(position) { MenuPositionProvider(position) },
                onDismissRequest = onDismissContextMenu,
                properties = PopupProperties(),
            ) {
                Card(
                    shape = RoundedCornerShape(4.dp),
                ) {
                    val time = log.raw.substring(log.time)
                    Column(Modifier.width(IntrinsicSize.Max)) {
                        Text(
                            text = "timeAfter=\"$time\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismissContextMenu()
                                    onAddTimeFilter(log.order, true)
                                }
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                        )
                        Text(
                            text = "timeBefore=\"$time\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismissContextMenu()
                                    onAddTimeFilter(log.order, false)
                                }
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

private class MenuPositionProvider(private val localPosition: Offset) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (anchorBounds.left + localPosition.x).toInt()
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val y = (anchorBounds.top + localPosition.y).toInt()
            .coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        return IntOffset(x, y)
    }
}

@Composable
private fun LogsVerticalScrollBar(lazyListState: LazyListState) {
    VsVerticalScrollbar(lazyListState)
}

@Composable
private fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
fun LazyListState.isScrollingUp(): State<Boolean> = produceState(initialValue = true) {
    var lastIndex = 0
    var lastScroll = Int.MAX_VALUE
    snapshotFlow {
        firstVisibleItemIndex to firstVisibleItemScrollOffset
    }.collect { (currentIndex, currentScroll) ->
        if (currentIndex != lastIndex || currentScroll != lastScroll) {
            value = currentIndex < lastIndex ||
                (currentIndex == lastIndex && currentScroll < lastScroll)
            lastIndex = currentIndex
            lastScroll = currentScroll
        }
    }
}
