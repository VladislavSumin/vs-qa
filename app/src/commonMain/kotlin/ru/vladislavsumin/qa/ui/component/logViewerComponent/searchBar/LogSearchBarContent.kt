package ru.vladislavsumin.qa.ui.component.logViewerComponent.searchBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.qa.ui.component.logViewerComponent.LogViewerViewModel
import ru.vladislavsumin.qa.ui.design.QaIconButton
import ru.vladislavsumin.qa.ui.design.QaToggleIconButton
import ru.vladislavsumin.qa.ui.theme.QaTheme

@Composable
internal fun LogsSearchBarContent(
    viewModel: LogViewerViewModel,
    state: State<LogSearchBarViewState>,
    focusRequester: FocusRequester,
    rootFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val state by state
    Row(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = state.searchRequest,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .handleHotKeys(viewModel, rootFocusRequester),
            singleLine = true,
            placeholder = { Text("Search...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            isError = state.isBadRegex,
            trailingIcon = {
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

private fun Modifier.handleHotKeys(
    viewModel: LogViewerViewModel,
    rootFocusRequester: FocusRequester,
): Modifier {
    return onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown) {
            when {
                event.isShiftPressed && event.key == Key.Enter -> {
                    viewModel.onClickPrevIndex()
                    true
                }

                event.key == Key.Enter -> {
                    viewModel.onClickNextIndex()
                    true
                }

                event.key == Key.Escape -> {
                    rootFocusRequester.requestFocus()
                    true
                }

                else -> false
            }
        } else {
            false
        }
    }
}
