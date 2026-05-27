package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.vladislavsumin.feature.logViewer.ui.utils.VsVerticalScrollbar

@Composable
@Suppress("LongMethod")
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
                        logItems(
                            logs = logs,
                            showRunNumbers = state.showRunNumbers,
                            header = { _, runNumber, meta ->
                                LogHeader(
                                    runNumber = runNumber,
                                    meta = meta,
                                    fontSize = state.logFontSize + 2,
                                    textSizeDp = textSizeDp,
                                )
                            },
                            record = { _, log ->
                                LogRecord(
                                    log = log,
                                    isSelected = log.order == state.currentSelectedItemOrder,
                                    stripDate = state.stripDate,
                                    fontSize = state.logFontSize,
                                    textSizeDp = textSizeDp,
                                )
                            },
                        )
                    }
                }
            }
            VerticalDivider(Modifier.padding(start = textSizeDp + 8.dp))
        }
        LogPreview(state = state, mainLazyListState = lazyListState)
        LogsVerticalScrollBar(lazyListState)
    }
}

@Composable
private fun ScrollToBottom(lazyListState: LazyListState) {
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
private fun LogsVerticalScrollBar(lazyListState: LazyListState) {
    VsVerticalScrollbar(lazyListState)
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
