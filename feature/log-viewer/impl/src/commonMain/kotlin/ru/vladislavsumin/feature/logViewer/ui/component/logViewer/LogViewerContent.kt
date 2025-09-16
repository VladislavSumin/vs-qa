package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.core.ui.hotkeyController.rememberHotkeyController
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.searchBar.LogsSearchBarContent
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsContent

@Composable
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    rootFocusRequester: FocusRequester,
    filterFocusRequester: FocusRequester,
    filterBarComponent: ComposeComponent,
    modifier: Modifier,
) {
    LaunchedEffect(rootFocusRequester) {
        rootFocusRequester.requestFocus()
    }
    val searchFocusRequester = remember { FocusRequester() }
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
                val logsState = remember {
                    derivedStateOf { state.value.logsViewState }
                }
                LogsContent(viewModel, logsState, Modifier.weight(1f))
                SidePanelContent(state)
            }
            filterBarComponent.Render(Modifier)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = QaTheme.colorScheme.surface,
                thickness = 1.5.dp,
            )
        }
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
                val data: String = state.value.logsViewState.logs.joinToString(separator = "\n") { it.raw }
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
