package ru.vladislavsumin.feature.logViewer.ui.component.savedFilters

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
@Suppress("MagicNumber")
internal fun SavedFiltersContent(
    viewModel: SavedFiltersViewModel,
    modifier: Modifier,
    onSavedFilterClick: (String) -> Unit,
) {
    val state = viewModel.state.collectAsState().value

    Column(modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(state.savedFilters, key = { it.name }) {
                if (state.editingFilterName == it.name) {
                    EditSavedFilterRow(viewModel, state, it)
                } else {
                    SavedFilterRow(viewModel, it, onSavedFilterClick)
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = QaTheme.colorScheme.surface,
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(thickness = 1.5.dp, color = QaTheme.colorScheme.surfaceVariant)
        Spacer(Modifier.height(4.dp))
        NewFilterSection(viewModel, state)
    }
}

@Composable
@Suppress("MagicNumber")
private fun NewFilterSection(viewModel: SavedFiltersViewModel, state: SavedFiltersViewState) {
    Column {
        Text(
            "New filter",
            style = MaterialTheme.typography.bodySmall,
            color = QaTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        if (state.saveError != null) {
            SaveError(state.saveError!!)
        }
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val nameFocus = remember { FocusRequester() }
            val contentFocus = remember { FocusRequester() }
            QaTextField(
                value = state.saveNewFilterName,
                onValueChange = viewModel::onSavedFilterNameChanged,
                placeholder = { Text("name") },
                modifier = Modifier.weight(1f)
                    .focusRequester(nameFocus)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                            contentFocus.requestFocus()
                            true
                        } else {
                            false
                        }
                    },
            )
            Spacer(Modifier.width(4.dp))
            QaTextField(
                value = state.saveNewFilterContent,
                onValueChange = viewModel::onSavedFilterContentChanged,
                placeholder = { Text("content") },
                modifier = Modifier.weight(5f)
                    .focusRequester(contentFocus)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Tab && event.type == KeyEventType.KeyDown && event.isShiftPressed) {
                            nameFocus.requestFocus()
                            true
                        } else {
                            false
                        }
                    },
            )
            Spacer(Modifier.width(4.dp))
            QaIconButton(
                onClick = viewModel::onClickSaveNewFilter,
                modifier = Modifier.hint("Save new filter"),
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "save")
            }
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun SavedFilterRow(
    viewModel: SavedFiltersViewModel,
    filter: SavedFiltersRepository.SavedFilter,
    onSavedFilterClick: (String) -> Unit,
) {
    // Используем detectTapGestures вместо clickable, что бы не перехватывать down-событие —
    // иначе дочерний SelectionContainer не сможет начать выделение текста drag'ом.
    // hoverable + indication — ручная реализация hover/ripple (то же, что делает clickable внутри).
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .pointerInput(filter) {
                detectTapGestures { onSavedFilterClick(filter.name) }
            }
            .hoverable(interactionSource)
            .indication(interactionSource, LocalIndication.current)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            filter.name,
            style = MaterialTheme.typography.bodySmall,
            color = QaTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(4.dp))
        SelectionContainer(Modifier.weight(5f)) {
            Text(viewModel.highlightSavedFilter(filter).colorize())
        }
        Spacer(Modifier.width(4.dp))
        QaIconButton(
            onClick = { viewModel.onStartEditingFilter(filter) },
            modifier = Modifier.hint("Edit saved filter"),
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "edit")
        }
        QaIconButton(
            onClick = { viewModel.onDeleteSavedFilter(filter) },
            modifier = Modifier.hint("Delete saved filter"),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "delete",
                tint = QaTheme.colorScheme.logError.primary,
            )
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun EditSavedFilterRow(
    viewModel: SavedFiltersViewModel,
    state: SavedFiltersViewState,
    filter: SavedFiltersRepository.SavedFilter,
) {
    Row(
        modifier = Modifier
            .background(QaTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val nameFocus = remember { FocusRequester() }
        val contentFocus = remember { FocusRequester() }
        QaTextField(
            value = state.editName,
            onValueChange = viewModel::onEditingFilterNameChanged,
            placeholder = { Text("name") },
            modifier = Modifier.weight(1f)
                .focusRequester(nameFocus)
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Tab && event.type == KeyEventType.KeyDown) {
                        contentFocus.requestFocus()
                        true
                    } else {
                        false
                    }
                },
        )
        Spacer(Modifier.width(4.dp))
        QaTextField(
            value = state.editContent,
            onValueChange = viewModel::onEditingFilterContentChanged,
            placeholder = { Text("content") },
            modifier = Modifier.weight(5f)
                .focusRequester(contentFocus)
                .onPreviewKeyEvent { event ->
                    if (
                        event.key == Key.Tab &&
                        event.type == KeyEventType.KeyDown &&
                        event.isShiftPressed
                    ) {
                        nameFocus.requestFocus()
                        true
                    } else {
                        false
                    }
                },
        )
        Spacer(Modifier.width(4.dp))
        QaIconButton(
            onClick = { viewModel.onClickSaveEditedFilter(filter) },
            modifier = Modifier.hint("Save changes"),
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "save changes")
        }
        QaIconButton(
            onClick = viewModel::onCancelEditingFilter,
            modifier = Modifier.hint("Cancel editing"),
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "cancel editing")
        }
    }
}

@Composable
private fun SaveError(message: String) {
    Text(
        message,
        color = QaTheme.colorScheme.logError.primary,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}
