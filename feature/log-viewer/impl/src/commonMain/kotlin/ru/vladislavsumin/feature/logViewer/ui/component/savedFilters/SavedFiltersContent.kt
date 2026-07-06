package ru.vladislavsumin.feature.logViewer.ui.component.savedFilters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
@Suppress("MagicNumber")
internal fun SavedFiltersContent(viewModel: SavedFiltersViewModel, modifier: Modifier) {
    val state = viewModel.state.collectAsState().value

    Column(modifier) {
        LazyColumn {
            items(state.savedFilters, key = { it.name }) {
                if (state.editingFilterName == it.name) {
                    EditSavedFilterRow(viewModel, state, it)
                } else {
                    SavedFilterRow(viewModel, it)
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
private fun SavedFilterRow(viewModel: SavedFiltersViewModel, filter: SavedFiltersRepository.SavedFilter) {
    Row {
        Text(filter.name, Modifier.weight(1f))
        SelectionContainer(Modifier.weight(5f)) {
            Text(viewModel.highlightSavedFilter(filter).colorize())
        }
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
            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
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
    Row {
        QaTextField(
            value = state.editName,
            onValueChange = viewModel::onEditingFilterNameChanged,
            placeholder = { Text("name") },
            modifier = Modifier.weight(1f),
        )
        QaTextField(
            value = state.editContent,
            onValueChange = viewModel::onEditingFilterContentChanged,
            placeholder = { Text("content") },
            modifier = Modifier.weight(5f),
        )
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
