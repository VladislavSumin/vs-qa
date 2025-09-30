package ru.vladislavsumin.feature.logViewer.ui.component.searchBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.HotkeyController
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.core.ui.hotkeyController.resetFocusOnEsc
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerViewModel

@Composable
@Suppress("LongMethod") // TODO
internal fun SearchBarContent(
    viewModel: LogViewerViewModel,
    state: State<SearchBarViewState>,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val state by state
    val hotkeyController = remember(viewModel) {
        HotkeyController(
            KeyModifier.Shift + Key.Enter to {
                viewModel.onClickPrevIndex()
                true
            },
            KeyModifier.None + Key.Enter to {
                viewModel.onClickNextIndex()
                true
            },
        )
    }
    Row(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QaTextField(
            value = state.searchRequest,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .resetFocusOnEsc()
                .weight(1f)
                .onPreviewKeyEvent(hotkeyController::invoke),
            maxLines = 1,
            placeholder = { Text("Search...") },
            leadingContent = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            isError = state.isBadRegex,
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    QaIconButton(
                        onClick = viewModel::onClickNextIndex,
                    ) { Icon(Icons.Default.ArrowDownward, null) }

                    QaIconButton(
                        onClick = viewModel::onClickPrevIndex,
                    ) { Icon(Icons.Default.ArrowUpward, null) }

                    // TODO написать нормально
                    val textMeasurer = rememberTextMeasurer()
                    val density = LocalDensity.current
                    val style = LocalTextStyle.current
                    val size = remember(textMeasurer, density, state.totalSearchResults) {
                        val count = "9".repeat(state.totalSearchResults.toString().length)
                        val testString = "$count of $count results"
                        val widthPx = textMeasurer.measure(testString, style).size.width
                        with(density) { widthPx.toDp() }
                    }

                    Text(
                        text = if (state.isBadRegex) {
                            "bad pattern"
                        } else {
                            "${state.currentSearchResultIndex + 1} of ${state.totalSearchResults} results"
                        },
                        Modifier
                            .padding(horizontal = 4.dp)
                            .defaultMinSize(minWidth = size),
                    )

                    QaToggleIconButton(
                        checked = state.isMatchCase,
                        onCheckedChange = viewModel::onClickSearchMatchCase,
                    ) { Text("Cc") }

                    QaToggleIconButton(
                        checked = state.isRegex,
                        onCheckedChange = viewModel::onClickSearchUseRegex,
                    ) { Text(".*") }
                }
            },
        )
    }
}
