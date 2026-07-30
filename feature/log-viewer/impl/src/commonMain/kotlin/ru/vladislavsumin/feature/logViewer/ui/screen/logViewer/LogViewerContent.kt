package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog
import ru.vladislavsumin.core.ui.hint.HintPlacement
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.core.ui.icons.QaIcons
import ru.vladislavsumin.feature.logViewer.ui.component.searchBar.SearchBarContent
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.Res
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_attach_mapping
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_copy
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_follow_tail
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_font_down
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_font_up
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_scroll_bottom
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_strip_date
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_side_tag_stats

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    searchFocusRequester: FocusRequester,
    filterBarComponent: ComposeComponent,
    dragAndDropOverlayComponent: ComposeComponent,
    logsComponent: ComposeComponent,
    tagStatComponent: ComposeComponent,
    modifier: Modifier,
) {
    Box(modifier = modifier) {
        val state = viewModel.state.collectAsState()
        val searchState = remember { derivedStateOf { state.value.searchState } }
        val showTagStat by remember { derivedStateOf { state.value.showTagStat } }
        // TODO вынести эту логику в viewModel.
        val showSideMenu = remember { mutableStateOf(false) }
        Column {
            SearchBarContent(viewModel, searchState, showSideMenu, searchFocusRequester)
            Row(Modifier.weight(1f)) {
                val modifier = Modifier
                    .weight(1f)
                    .padding(start = 2.dp)
                    .clip(QaTheme.shapes.extraSmall)
                    .background(QaTheme.colorScheme.background3)
                // TODO скрол не должен сбрасываться а тут будет.
                if (showTagStat) {
                    tagStatComponent.Render(modifier)
                } else {
                    logsComponent.Render(modifier)
                }
                // TODO сделать нормальные расширения для адаптивной верстки
                val withDp = with(LocalDensity.current) {
                    LocalWindowInfo.current.containerSize.width.toDp()
                }
                if (withDp > 600.dp || showSideMenu.value) {
                    SidePanelContent(viewModel, state)
                }
            }
            filterBarComponent.Render(Modifier)
        }

        dragAndDropOverlayComponent.Render(Modifier)
    }
}

@Composable
@Suppress("LongMethod")
private fun SidePanelContent(viewModel: LogViewerViewModel, state: State<LogViewerViewState>) {
    val clipboard = LocalClipboardManager.current
    Column(
        Modifier.fillMaxHeight().width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QaIconButton(
            onClick = {
                // TODO провести через вью модель.
                val data: String = state.value.logsViewState.rawLogs.joinToString(separator = "\n") { it.raw }
                clipboard.setText(AnnotatedString(data))
            },
            Modifier
                .hint(stringResource(Res.string.log_viewer_side_copy), placement = HintPlacement.LEFT)
                .padding(4.dp),
        ) { Icon(QaIcons.ContentCopy, null) }
        if (state.value.isMappingSupported) {
            if (state.value.showSelectMappingDialog) {
                FilePickerDialog(onCloseRequest = viewModel::onSelectMappingDialogResult)
            }
            QaToggleIconButton(
                checked = state.value.isMappingApplied,
                onCheckedChange = { viewModel.onClickMappingButton() },
                Modifier
                    .hint(stringResource(Res.string.log_viewer_side_attach_mapping), placement = HintPlacement.LEFT)
                    .padding(4.dp),
            ) {
                Icon(QaIcons.FilePresent, null)
            }
        }
        QaToggleIconButton(
            checked = state.value.isStripDate,
            onCheckedChange = { viewModel.onClickStripDate() },
            Modifier
                .hint(stringResource(Res.string.log_viewer_side_strip_date), placement = HintPlacement.LEFT)
                .padding(4.dp),
        ) {
            Icon(QaIcons.CalendarClock, null)
        }
        QaToggleIconButton(
            checked = state.value.showTagStat,
            onCheckedChange = { viewModel.onClickShowTagStat() },
            Modifier
                .hint(stringResource(Res.string.log_viewer_side_tag_stats), placement = HintPlacement.LEFT)
                .padding(4.dp),
        ) {
            Icon(QaIcons.BarChart_4Bars, null)
        }
        QaIconButton(
            onClick = { viewModel.onClickFontUp() },
            Modifier
                .hint(stringResource(Res.string.log_viewer_side_font_up), placement = HintPlacement.LEFT)
                .padding(4.dp),
        ) {
            Icon(QaIcons.TextIncrease, null)
        }
        Text(
            state.value.logsViewState.logFontSize.toString(),
            Modifier.padding(4.dp),
        )
        QaIconButton(
            onClick = { viewModel.onClickFontDown() },
            Modifier
                .hint(stringResource(Res.string.log_viewer_side_font_down), placement = HintPlacement.LEFT)
                .padding(4.dp),
        ) {
            Icon(QaIcons.TextDecrease, null)
        }
        Spacer(Modifier.weight(1f))
        if (state.value.isFollowTailSupported) {
            QaToggleIconButton(
                checked = state.value.logsViewState.followTail,
                onCheckedChange = { viewModel.onClickFollowTail() },
                Modifier
                    .hint(stringResource(Res.string.log_viewer_side_follow_tail), placement = HintPlacement.LEFT)
                    .padding(4.dp),
            ) {
                Icon(QaIcons.KeyboardDoubleArrowDown, null)
            }
        }
        QaIconButton(
            onClick = { viewModel.onClickScrollToBottom() },
            Modifier
                .hint(stringResource(Res.string.log_viewer_side_scroll_bottom), placement = HintPlacement.LEFT)
                .padding(4.dp),
        ) { Icon(QaIcons.ArrowDownward, null) }
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
