package ru.vladislavsumin.feature.logViewer.ui.component.logViewer.searchBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.core.ui.hotkeyController.rememberHotkeyController
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerViewModel

@Composable
@Suppress("LongMethod") // TODO
internal fun LogsSearchBarContent(
    viewModel: LogViewerViewModel,
    state: State<LogSearchBarViewState>,
    focusRequester: FocusRequester,
    rootFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val state by state
    val hotkeyController = rememberHotkeyController(
        KeyModifier.Shift + Key.Enter to {
            viewModel.onClickPrevIndex()
            true
        },
        KeyModifier.None + Key.Enter to {
            viewModel.onClickNextIndex()
            true
        },
        KeyModifier.None + Key.Escape to { rootFocusRequester.requestFocus() },
    )
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

                    Text(
                        text = if (state.isBadRegex) {
                            "bad pattern"
                        } else {
                            "${state.currentSearchResultIndex + 1} of ${state.totalSearchResults} results"
                        },
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
