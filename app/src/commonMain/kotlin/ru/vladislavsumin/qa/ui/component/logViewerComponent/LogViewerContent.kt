package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.qa.ui.utils.colorize

@Composable
internal fun LogViewerContent(viewModel: LogViewerViewModel, modifier: Modifier) {
    Surface {
        val state by viewModel.state.collectAsState()
        val logs = state.logs
        val textSizeDp = measureTextWidth(
            " ".repeat(state.maxLogNumberDigits),
            MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
        )
        Row(modifier) {
            val lazyListState = rememberLazyListState()
            Box(Modifier.weight(1f)) {
                SelectionContainer {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(logs, { it.order }) {
                            Box {
                                DisableSelection {
                                    Text(
                                        text = it.order.toString(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                    )
                                }
                                Text(
                                    text = it.colorize(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(start = textSizeDp + 6.dp),
                                )
                            }
                        }
                    }
                }
                VerticalDivider(Modifier.padding(start = textSizeDp + 2.dp))
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(lazyListState),
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}