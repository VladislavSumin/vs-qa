package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.utils.VsVerticalScrollbar

@Composable
internal fun FilterHintContent(viewModel: FilterHintViewModel, modifier: Modifier, offsetX: Float,) {
    when (val state = viewModel.state.collectAsState().value) {
        is FilterHintViewState.Hidden -> Unit
        is FilterHintViewState.Show -> HintContent(viewModel, state, modifier, offsetX)
    }
}

@Composable
private fun HintContent(
    viewModel: FilterHintViewModel,
    state: FilterHintViewState.Show,
    modifier: Modifier,
    cursorOffset: Float,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Popup(
        popupPositionProvider = remember(cursorOffset) { HintPopupPositionProvider(cursorOffset) },
        onDismissRequest = viewModel::onCloseRequest,
        properties = PopupProperties(),
    ) {
        // TODO поработать над логикой вычисления размеров.
        Card(
            modifier = modifier.size(400.dp, 250.dp),
            shape = RoundedCornerShape(2.dp),
        ) {
            Row {
                val lazyListState = rememberLazyListState()
                LazyColumn(Modifier.weight(1f), state = lazyListState) {
                    items(state.items, key = { it.text }) {
                        val modifier = if (it.key == state.selectedItemKey) {
                            Modifier.background(QaTheme.colorScheme.logHighlightSelected)
                        } else {
                            Modifier
                        }
                        Row(
                            modifier
                                .clickable(onClick = { viewModel.onAcceptHint(it) })
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            val span = buildAnnotatedString {
                                append(it.text)
                                it.highlights.forEach { highlight ->
                                    this.addStyle(
                                        SpanStyle(fontWeight = FontWeight.Bold),
                                        highlight.first,
                                        highlight.last + 1,
                                    )
                                }
                            }
                            Text(span)
                            Spacer(modifier.weight(1f))
                            if (it.hint != null) {
                                Text(it.hint, color = QaTheme.colorScheme.logTrace.primary)
                            }
                        }
                    }
                }
                if (lazyListState.canScrollForward || lazyListState.canScrollBackward) {
                    VsVerticalScrollbar(lazyListState)
                }
            }
        }
    }
}

@Suppress("MagicNumber")
private class HintPopupPositionProvider(private val offsetX: Float,) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(
        x = anchorBounds.left + offsetX.toInt(),
        y = anchorBounds.top - popupContentSize.height - 16,
    )
}
