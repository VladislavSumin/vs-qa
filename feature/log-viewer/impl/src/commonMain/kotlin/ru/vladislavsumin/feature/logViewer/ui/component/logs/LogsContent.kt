package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.TextSelectionSeparator
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
internal fun LogsContent(
    events: ReceiveChannel<LogsEvents>,
    state: StateFlow<LogsViewState>,
    modifier: Modifier,
) {
    val state = state.collectAsState()
    val logs = state.value.logs
    val maxLogNumberDigits = state.value.maxLogNumberDigits
    val textSizeDp = measureTextWidth(
        " ".repeat(maxLogNumberDigits),
        MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    )
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        events.receiveAsFlow().collect { event ->
            when (event) {
                is LogsEvents.ScrollToIndex -> lazyListState.scrollToItem(event.index)
            }
        }
    }

    Row(modifier) {
        Box(Modifier.weight(1f)) {
            SelectionContainer {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(logs, { it.order }) {
                        Record(it, maxLogNumberDigits, textSizeDp)
                    }
                }
            }
            VerticalDivider(Modifier.padding(start = textSizeDp + 8.dp))
        }
        LogsVerticalScrollBar(lazyListState)
    }
}

@Composable
private fun Record(
    log: LogRecord,
    maxLogNumberDigits: Int,
    textSizeDp: Dp,
) {
    Box {
        DisableSelection {
            Text(
                text = "${" ".repeat(maxLogNumberDigits - log.order.toString().length)}${log.order}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Text(
            text = log.colorize(),
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
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(lazyListState),
        style = LocalScrollbarStyle.current.copy(
            hoverColor = QaTheme.colorScheme.onSurface,
            unhoverColor = QaTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.fillMaxHeight(),
    )
}

@Composable
private fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}
