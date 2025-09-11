package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.core.ui.hotkeyController.rememberHotkeyController
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.searchBar.LogsSearchBarContent
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    modifier: Modifier,
) {
    val rootFocusRequester = remember { FocusRequester() }
    LaunchedEffect(rootFocusRequester) {
        rootFocusRequester.requestFocus()
    }
    val searchFocusRequester = remember { FocusRequester() }
    val filterFocusRequester = remember { FocusRequester() }
    val hotkeyController = rememberHotkeyController(
        KeyModifier.Shift + KeyModifier.Command + Key.F to { filterFocusRequester.requestFocus() },
        KeyModifier.Command + Key.F to { searchFocusRequester.requestFocus() },
    )
    Surface(
        modifier = modifier
            .focusRequester(rootFocusRequester)
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .onPreviewKeyEvent(hotkeyController::invoke),
    ) {
        val state = viewModel.state.collectAsState()
        val searchState = derivedStateOf { state.value.searchState }
        Column {
            LogsSearchBarContent(viewModel, searchState, searchFocusRequester, rootFocusRequester)
            Row(Modifier.weight(1f)) {
                LogsContent(viewModel, state, Modifier.weight(1f))
                SidePanelContent(state)
            }
            LogsFilter(viewModel, state, filterFocusRequester, rootFocusRequester)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = QaTheme.colorScheme.surface,
                thickness = 1.5.dp,
            )
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
        val hotkeyController = rememberHotkeyController(
            KeyModifier.None + Key.Escape to { rootFocusRequester.requestFocus() },
        )
        QaTextField(
            value = state.value.filter,
            onValueChange = viewModel::onFilterChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .onKeyEvent(hotkeyController::invoke),
            isError = !state.value.isFilterValid,
            placeholder = { Text("Filter...") },
            leadingContent = {
                Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null)
            },
        )
    }
}

@Composable
private fun SidePanelContent(
    state: State<LogViewerViewState>,
) {
    val clipboard = LocalClipboardManager.current
    Column(
        Modifier.fillMaxHeight().width(IntrinsicSize.Min).background(QaTheme.colorScheme.surfaceVariant),
    ) {
        Divider(color = QaTheme.colorScheme.surface, thickness = 1.5.dp)
        QaIconButton(
            onClick = {
                // TODO провести через вью модель.
                val data: String = state.value.logs.joinToString(separator = "\n") { it.raw }
                clipboard.setText(AnnotatedString(data))
            },
            Modifier.padding(4.dp),
        ) {
            Icon(Icons.Default.CopyAll, null)
        }
        Spacer(Modifier.weight(1f))
        Divider(color = QaTheme.colorScheme.surface, thickness = 1.5.dp)
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
