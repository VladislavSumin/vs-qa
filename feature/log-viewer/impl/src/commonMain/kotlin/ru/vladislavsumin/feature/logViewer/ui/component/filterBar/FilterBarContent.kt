package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.core.ui.hotkeyController.HotkeyController
import ru.vladislavsumin.core.ui.hotkeyController.resetFocusOnEsc
import ru.vladislavsumin.core.ui.icons.QaIcons
import ru.vladislavsumin.feature.logViewer.ui.component.filterHint.FilterHintComponent
import ru.vladislavsumin.feature.logViewer.ui.component.savedFilters.SavedFiltersComponent
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.Res
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_filter_help
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_filter_help_cd
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_filter_placeholder
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_filter_saved
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_filter_saved_cd

@Composable
internal fun FilterBarContent(
    viewModel: FilterBarViewModel,
    filterHintComponent: FilterHintComponent,
    savedFiltersComponent: SavedFiltersComponent,
    filterHintHotkeyController: HotkeyController,
    focusRequester: FocusRequester,
    modifier: Modifier,
) {
    val state = viewModel.state.collectAsState()
    val showSavedFilters by remember { derivedStateOf { state.value.showSavedFilters } }
    Column(modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
        if (showSavedFilters) {
            savedFiltersComponent.Render(Modifier)
        }
        FilterField(viewModel, state, filterHintComponent, filterHintHotkeyController, focusRequester)
    }
}

@Composable
private fun FilterField(
    viewModel: FilterBarViewModel,
    state: State<FilterBarViewState>,
    filterHintComponent: FilterHintComponent,
    filterHintHotkeyController: HotkeyController,
    focusRequester: FocusRequester,
) {
    val state = state.value
    var cursorPosition by remember { mutableFloatStateOf(0f) }

    if (state.error != null) {
        Text(text = state.error, color = QaTheme.colorScheme.logError.primary)
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
        placeholder = { Text(stringResource(Res.string.log_viewer_filter_placeholder)) },
        centerContent = { filterHintComponent.Render(Modifier, cursorPosition) },
        leadingContent = { Icon(imageVector = QaIcons.FilterAlt, contentDescription = null) },
        trailingContent = {
            QaToggleIconButton(
                checked = state.showSavedFilters,
                onCheckedChange = { viewModel.onClickSavedFilters() },
                modifier = Modifier.hint(stringResource(Res.string.log_viewer_filter_saved)),
            ) {
                Icon(
                    imageVector = QaIcons.Bookmarks,
                    contentDescription = stringResource(Res.string.log_viewer_filter_saved_cd),
                )
            }
            HelpButton(viewModel, state)
        },
    )
}

@Composable
private fun HelpButton(viewModel: FilterBarViewModel, state: FilterBarViewState) {
    QaIconButton(
        onClick = viewModel::onClickHelpButton,
        modifier = Modifier
            .padding(start = 2.dp)
            .hint(stringResource(Res.string.log_viewer_filter_help)),
    ) {
        DropdownMenu(
            expanded = state.showHelpMenu,
            onDismissRequest = viewModel::onDismissHelpMenu,
            containerColor = QaTheme.colorScheme.background1,
        ) { HelpMenuContent() }
        Icon(

            imageVector = QaIcons.Help,
            contentDescription = stringResource(Res.string.log_viewer_filter_help_cd),
        )
    }
}
