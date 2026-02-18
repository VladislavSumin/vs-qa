package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.HotkeyController
import ru.vladislavsumin.core.ui.hotkeyController.resetFocusOnEsc
import ru.vladislavsumin.feature.logViewer.ui.component.filterHint.FilterHintComponent
import ru.vladislavsumin.feature.logViewer.ui.utils.addStyle

@Composable
internal fun FilterBarContent(
    viewModel: FilterBarViewModel,
    filterHintComponent: FilterHintComponent,
    filterHintHotkeyController: HotkeyController,
    focusRequester: FocusRequester,
    modifier: Modifier,
) {
    Column(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        SavedFilters(viewModel)
        FilterField(viewModel, filterHintComponent, filterHintHotkeyController, focusRequester)
    }
}

@Composable
@Suppress("MagicNumber")
private fun SavedFilters(viewModel: FilterBarViewModel) {
    val state = viewModel.state.collectAsState().value.savedFiltersState
    if (!state.showSavedFilters) return

    LazyColumn {
        items(state.savedFilters, key = { it.name }) {
            Row {
                Text(it.name, Modifier.weight(1f))
                Text(viewModel.highlightSavedFilter(it).colorize(), Modifier.weight(5f))
                QaIconButton(onClick = { viewModel.onDeleteSavedFilter(it) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
                }
            }
        }
    }

    Row {
        QaTextField(
            value = state.saveNewFilterName,
            onValueChange = viewModel::onSavedFilterNameChanged,
            placeholder = { Text("name") },
            modifier = Modifier.weight(1f),
        )
        QaTextField(
            value = state.saveNewFilterContent,
            onValueChange = viewModel::onSavedFilterContentChanged,
            placeholder = { Text("content") },
            modifier = Modifier.weight(5f),
        )
        QaIconButton(onClick = viewModel::onClickSaveNewFilter) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "save")
        }
    }
}

@Composable
private fun FilterField(
    viewModel: FilterBarViewModel,
    filterHintComponent: FilterHintComponent,
    filterHintHotkeyController: HotkeyController,
    focusRequester: FocusRequester,
) {
    var cursorPosition by remember { mutableFloatStateOf(0f) }

    val state by viewModel.state.collectAsState()
    if (state.error != null) {
        Text(text = state.error.toString(), color = QaTheme.colorScheme.logError.primary)
    }

    QaTextField(
        value = state.field.copy(annotatedString = state.highlight.colorize()),
        onValueChange = viewModel::onFilterChange,
        modifier = Modifier
            .focusRequester(focusRequester)
            .resetFocusOnEsc()
            .onPreviewKeyEvent(filterHintHotkeyController::invoke),
        isError = state.error != null,
        onTextLayout = { layout ->
            cursorPosition = layout.getHorizontalPosition(
                offset = state.field.selection.start - state.predictionWordLength,
                usePrimaryDirection = true,
            )
        },
        placeholder = { Text("Filter...") },
        centerContent = { filterHintComponent.Render(Modifier, cursorPosition) },
        leadingContent = { Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null) },
        trailingContent = {
            QaToggleIconButton(
                checked = state.savedFiltersState.showSavedFilters,
                onCheckedChange = { viewModel.onClickSavedFilters() },
            ) {
                Icon(imageVector = Icons.Default.Bookmarks, contentDescription = "saved filters")
            }
            HelpButton(viewModel, state)
        },
    )
}

@Composable
private fun HelpButton(
    viewModel: FilterBarViewModel,
    state: FilterBarViewState,
) {
    QaIconButton(onClick = viewModel::onClickHelpButton) {
        DropdownMenu(
            expanded = state.showHelpMenu,
            onDismissRequest = viewModel::onDismissHelpMenu,
        ) { HelpMenuContent() }
        Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = "help")
    }
}

@Composable
private fun FilterRequestParser.RequestHighlight.colorize(): AnnotatedString {
    return when (this) {
        is FilterRequestParser.RequestHighlight.InvalidSyntax -> buildAnnotatedString { append(raw) }
        is FilterRequestParser.RequestHighlight.Success -> buildAnnotatedString {
            append(raw)
            keywords.forEach { range ->
                addStyle(SpanStyle(color = QaTheme.colorScheme.onSurfaceVariant), range)
            }
        }
    }
}
