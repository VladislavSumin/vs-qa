package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.ui.design.QaToggleIconButton
import ru.vladislavsumin.qa.ui.theme.QaTheme
import ru.vladislavsumin.qa.ui.utils.colorize

@Composable
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    memoryIndicator: ComposeComponent,
    modifier: Modifier,
) {
    Surface(modifier = modifier) {
        val state = viewModel.state.collectAsState()
        Column {
            LogsSearch(viewModel, state)
            LogsContent(state, Modifier.weight(1f))
            LogsFilter(viewModel, state)
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = state.value.filter,
            onValueChange = viewModel::onFilterChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Filter...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null)
            },
            trailingIcon = {
                QaToggleIconButton(
                    checked = state.value.isFilterUseRegex,
                    onCheckedChange = viewModel::onClickFilterUseRegex,
                ) { Text(".*") }
            }
        )
    }
}

@Composable
private fun LogsSearch(
    viewModel: LogViewerViewModel,
    state: State<LogViewerViewState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = state.value.search,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                QaToggleIconButton(
                    checked = state.value.isSearchUseRegex,
                    onCheckedChange = viewModel::onClickSearchUseRegex,
                ) { Text(".*") }
            }
        )
    }
}

@Composable
private fun LogsContent(
    state: State<LogViewerViewState>,
    modifier: Modifier,
) {
    val logs = state.value.logs
    val textSizeDp = measureTextWidth(
        " ".repeat(state.value.maxLogNumberDigits),
        MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    )
    val lazyListState = rememberLazyListState()
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