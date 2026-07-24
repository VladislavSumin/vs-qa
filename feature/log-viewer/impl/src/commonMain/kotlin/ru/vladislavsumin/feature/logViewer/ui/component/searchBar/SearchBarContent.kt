package ru.vladislavsumin.feature.logViewer.ui.component.searchBar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.ui.QaTextField
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.core.ui.hotkeyController.HotkeyController
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.core.ui.hotkeyController.resetFocusOnEsc
import ru.vladislavsumin.core.ui.icons.QaIcons
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerViewModel
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.Res
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_bad_pattern
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_case_sensitive
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_next
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_placeholder
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_prev
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_toggle_side_panel
import ru.vladislavsumin.feature.log_viewer.impl.generated.resources.log_viewer_search_use_regex

@Composable
internal fun SearchBarContent(
    viewModel: LogViewerViewModel,
    state: State<SearchBarViewState>,
    showSideMenu: MutableState<Boolean>,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val state by state
    val hotkeyController = remember(viewModel) {
        HotkeyController(
            KeyModifier.Shift + Key.Enter to {
                viewModel.onClickPrevIndex()
                true
            },
            KeyModifier.None + Key.Enter to {
                viewModel.onClickNextIndex()
                true
            },
        )
    }
    Row(
        modifier
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QaTextField(
            value = state.searchRequest,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier
                .focusRequester(focusRequester)
                .resetFocusOnEsc()
                .weight(1f)
                .onPreviewKeyEvent(hotkeyController::invoke),
            maxLines = 1,
            placeholder = { Text(stringResource(Res.string.log_viewer_search_placeholder)) },
            leadingContent = { Icon(imageVector = QaIcons.Search, contentDescription = null) },
            isError = state.isBadRegex,
            trailingContent = { TrailingButtons(viewModel, state, showSideMenu) },
        )
    }
}

@Composable
private fun TrailingButtons(
    viewModel: LogViewerViewModel,
    state: SearchBarViewState,
    showSideMenu: MutableState<Boolean>,
) {
    QaIconButton(
        onClick = viewModel::onClickNextIndex,
        modifier = Modifier.hint(stringResource(Res.string.log_viewer_search_next)),
    ) { Icon(QaIcons.ArrowDownward, null) }

    QaIconButton(
        onClick = viewModel::onClickPrevIndex,
        modifier = Modifier
            .padding(start = 2.dp)
            .hint(stringResource(Res.string.log_viewer_search_prev)),
    ) { Icon(QaIcons.ArrowUpward, null) }

    // TODO написать нормально
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = LocalTextStyle.current
    val size = remember(textMeasurer, density, state.totalSearchResults) {
        val count = "9".repeat(state.totalSearchResults.toString().length)
        val testString = "$count / $count"
        val widthPx = textMeasurer.measure(testString, style).size.width
        with(density) { widthPx.toDp() }
    }

    Text(
        text = if (state.isBadRegex) {
            stringResource(Res.string.log_viewer_search_bad_pattern)
        } else {
            "${state.currentSearchResultIndex + 1} / ${state.totalSearchResults}"
        },
        Modifier
            .padding(horizontal = 4.dp)
            .defaultMinSize(minWidth = size),
    )

    QaToggleIconButton(
        checked = state.isMatchCase,
        onCheckedChange = viewModel::onClickSearchMatchCase,
        modifier = Modifier
            .padding(start = 2.dp)
            .hint(stringResource(Res.string.log_viewer_search_case_sensitive)),
    ) { Icon(QaIcons.MatchCase, null) }

    QaToggleIconButton(
        checked = state.isRegex,
        onCheckedChange = viewModel::onClickSearchUseRegex,
        modifier = Modifier
            .padding(start = 2.dp)
            .hint(stringResource(Res.string.log_viewer_search_use_regex)),
    ) { Icon(QaIcons.RegularExpression, null) }

    val withDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    if (withDp <= 600.dp) {
        QaToggleIconButton(
            checked = showSideMenu.value,
            onCheckedChange = { showSideMenu.value = it },
            modifier = Modifier
                .padding(start = 2.dp)
                .hint(stringResource(Res.string.log_viewer_search_toggle_side_panel)),
        ) { Icon(QaIcons.MoreVert, null) }
    }
}
