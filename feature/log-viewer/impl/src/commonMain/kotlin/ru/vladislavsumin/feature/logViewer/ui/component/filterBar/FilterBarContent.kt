package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.core.ui.hotkeyController.rememberHotkeyController
import ru.vladislavsumin.feature.logViewer.ui.utils.addStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterBarContent(
    viewModel: FilterBarViewModel,
    onFocusLost: () -> Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier,
) {
    Row(
        modifier
            .background(QaTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val hotkeyController = rememberHotkeyController(
            KeyModifier.None + Key.Escape to onFocusLost,
        )
        val state by viewModel.state.collectAsState()
        QaTextField(
            value = state.field.copy(
                annotatedString = state.highlight.colorize(),
            ),
            onValueChange = viewModel::onFilterChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .onKeyEvent(hotkeyController::invoke),
            isError = state.isError,
            placeholder = { Text("Filter...") },
            leadingContent = {
                Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null)
            },
            trailingContent = {
                QaIconButton(
                    onClick = viewModel::onClickHelpButton,
                ) {
                    DropdownMenu(
                        expanded = state.showHelpMenu,
                        onDismissRequest = viewModel::onDismissHelpMenu,
                    ) {
                        HelpMenuContent()
                    }
                    Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = "help")
                }
            },
        )
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
