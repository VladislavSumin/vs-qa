package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.qa.ui.component.logViewerComponent.searchBar.LogsSearchBarContent
import ru.vladislavsumin.qa.ui.utils.colorize

@Composable
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    memoryIndicator: ComposeComponent,
    modifier: Modifier,
) {
    val rootFocusRequester = remember { FocusRequester() }
    LaunchedEffect(rootFocusRequester) {
        rootFocusRequester.requestFocus()
    }
    val searchFocusRequester = remember { FocusRequester() }
    val filterFocusRequester = remember { FocusRequester() }
    Surface(
        modifier = modifier
            .focusRequester(rootFocusRequester)
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when {
                        event.isShiftPressed && event.isMetaPressed && event.key == Key.F -> {
                            filterFocusRequester.requestFocus()
                        }

                        event.isMetaPressed && event.key == Key.F -> searchFocusRequester.requestFocus()
                        else -> false
                    }
                } else {
                    false
                }
            },
    ) {
        val state = viewModel.state.collectAsState()
        val searchState = derivedStateOf { state.value.searchState }
        Column {
            LogsSearchBarContent(viewModel, searchState, searchFocusRequester, rootFocusRequester)
            LogsContent(viewModel, state, Modifier.weight(1f))
            LogsFilter(viewModel, state, filterFocusRequester, rootFocusRequester)
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = QaTheme.colorScheme.surface,
                thickness = 1.5.dp,
            )
            Row(
                Modifier.background(QaTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f))
                Text(
                    text = "Total records: ${state.value.logs.size}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(
                        vertical = 2.dp,
                        horizontal = 8.dp,
                    ),
                )
                memoryIndicator.Render(Modifier)
            }
        }
    }
}

@Composable
private fun LogsFilter(
    viewModel: LogViewerViewModel,
    state: State<LogViewerViewState>,
    focusRequester: FocusRequester,
    rootFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QaTextField(
            value = state.value.filter,
            onValueChange = viewModel::onFilterChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {
                            event.key == Key.Escape -> {
                                rootFocusRequester.requestFocus()
                                true
                            }

                            else -> false
                        }
                    } else {
                        false
                    }
                },
            isError = !state.value.isFilterValid,
            placeholder = { Text("Filter...") },
            leadingContent = {
                Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null)
            },
        )
    }
}

@Composable
@Suppress("LongMethod") // TODO
private fun LogsContent(
    viewModel: LogViewerViewModel,
    state: State<LogViewerViewState>,
    modifier: Modifier,
) {
    val logs = state.value.logs
    val maxLogNumberDigits = state.value.maxLogNumberDigits
    val textSizeDp = measureTextWidth(
        " ".repeat(maxLogNumberDigits),
        MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    )
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        viewModel.events.receiveAsFlow().collect { event ->
            when (event) {
                is LogViewerEvents.ScrollToIndex -> lazyListState.scrollToItem(event.index)
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .map { (it.firstOrNull()?.index ?: 0) to (it.lastOrNull()?.index ?: 0) }
            .distinctUntilChanged()
            .collectLatest { (f, l) -> viewModel.onVisibleItemsChanged(f, l) }
    }

    Row(modifier) {
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
                                    text = "${" ".repeat(maxLogNumberDigits - it.order.toString().length)}${it.order}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier,
                                )
                            }
                            Text(
                                text = it.colorize(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth() // Что бы выделение работало после конца текста
                                    .padding(start = textSizeDp + 9.dp),
                            )
                            TextSelectionSeparator()
                        }
                    }
                }
            }
            VerticalDivider(Modifier.padding(start = textSizeDp + 4.dp))
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(lazyListState),
            style = LocalScrollbarStyle.current.copy(
                hoverColor = QaTheme.colorScheme.onSurface,
                unhoverColor = QaTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxHeight(),
        )
    }
}

@Composable
private fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

/**
 * Invisible text to separate text blocks in selected text.
 *
 * Workaround for [this issue](https://issuetracker.google.com/issues/285036739)
 */
@Composable
fun TextSelectionSeparator(text: String = "\n") {
    Text(
        modifier = Modifier.sizeIn(maxWidth = 0.dp, maxHeight = 0.dp),
        text = text,
    )
}
