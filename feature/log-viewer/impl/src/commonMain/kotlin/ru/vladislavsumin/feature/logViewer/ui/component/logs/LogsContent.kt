package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.TextSelectionSeparator
import ru.vladislavsumin.feature.logViewer.ui.utils.VsVerticalScrollbar
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
internal fun LogsContent(
    onFirstVisibleIndexChange: (Int) -> Unit,
    events: ReceiveChannel<LogsEvents>,
    state: StateFlow<LogsViewState>,
    modifier: Modifier,
) {
    val state by state.collectAsState()
    val logs = state.logs
    val maxLogNumberDigits = state.maxLogNumberDigits
    val textSizeDp = measureTextWidth(
        " ".repeat(maxLogNumberDigits),
        MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
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
                                        textSizeDp,
                                    )
                                }
                            }
                            items(sectionInfo.logs, { it.order }) {
                                Record(it, it.order == state.currentSelectedItemOrder, textSizeDp)
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
private fun Header(
    runNumber: Int,
    meta: Map<String, String>?,
    textSizeDp: Dp,
) {
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
        Text(text, Modifier.padding(start = textSizeDp + 13.dp + 8.dp, end = 4.dp))
    }
}

@Composable
private fun Record(
    log: LogRecord,
    isSelected: Boolean,
    textSizeDp: Dp,
) {
    Box {
        DisableSelection {
            Text(
                // order нумеруется с 0, но визуально записи более правильно нумеровать с единицы.
                text = "${log.order + 1}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .defaultMinSize(minWidth = textSizeDp),
            )
        }
        Text(
            text = log.colorize(isSelected),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth() // Что бы выделение работало после конца текста
                .padding(start = textSizeDp + 13.dp),
        )
        TextSelectionSeparator()
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
fun LazyListState.isScrollingUp(): State<Boolean> {
    return produceState(initialValue = true) {
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
}
