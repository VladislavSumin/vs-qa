package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsComponent
import ru.vladislavsumin.feature.logViewer.ui.component.searchBar.SearchBarContent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    searchFocusRequester: FocusRequester,
    filterBarComponent: ComposeComponent,
    dragAndDropOverlayComponent: ComposeComponent,
    logsComponent: LogsComponent,
    modifier: Modifier,
) {
    Surface(modifier = modifier) {
        val state = viewModel.state.collectAsState()
        val searchState = remember { derivedStateOf { state.value.searchState } }
        Column {
            SearchBarContent(viewModel, searchState, searchFocusRequester)
            Row(Modifier.weight(1f)) {
                logsComponent.Render(Modifier.weight(1f))
                // TODO сделать нормальные расширения для адаптивной верстки
                val withDp = with(LocalDensity.current) {
                    LocalWindowInfo.current.containerSize.width.toDp()
                }
                if (withDp > 600.dp) {
                    SidePanelContent(viewModel, state)
                }
            }
            filterBarComponent.Render(Modifier)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = QaTheme.colorScheme.surface,
                thickness = 1.5.dp,
            )
        }

        dragAndDropOverlayComponent.Render(Modifier)
    }
}

@Composable
private fun SidePanelContent(
    viewModel: LogViewerViewModel,
    state: State<LogViewerViewState>,
) {
    val clipboard = LocalClipboardManager.current
    Column(
        Modifier.fillMaxHeight().width(IntrinsicSize.Min).background(QaTheme.colorScheme.surfaceVariant),
    ) {
        HorizontalDivider(color = QaTheme.colorScheme.surface, thickness = 1.5.dp)
        QaIconButton(
            onClick = {
                // TODO провести через вью модель.
                val data: String = state.value.logsViewState.rawLogs.joinToString(separator = "\n") { it.raw }
                clipboard.setText(AnnotatedString(data))
            },
            Modifier.padding(4.dp),
        ) { Icon(Icons.Default.CopyAll, null) }
        if (state.value.showSelectMappingDialog) {
            FilePickerDialog(onCloseRequest = viewModel::onSelectMappingDialogResult)
        }
        QaToggleIconButton(
            checked = state.value.isMappingApplied,
            onCheckedChange = { viewModel.onClickMappingButton() },
            Modifier.padding(4.dp),
        ) {
            Icon(Icons.Default.FilePresent, null)
        }
        Spacer(Modifier.weight(1f))
        QaIconButton(
            onClick = { viewModel.onClickScrollToBottom() },
            Modifier.padding(4.dp),
        ) { Icon(Icons.Default.ArrowDownward, null) }
        HorizontalDivider(color = QaTheme.colorScheme.surface, thickness = 1.5.dp)
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
