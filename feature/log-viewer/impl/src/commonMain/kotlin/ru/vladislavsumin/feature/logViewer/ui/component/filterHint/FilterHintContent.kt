package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun FilterHintContent(
    viewModel: FilterHintViewModel,
    modifier: Modifier,
) {
    when (val state = viewModel.state.collectAsState().value) {
        is FilterHintViewState.Hidden -> Unit
        is FilterHintViewState.Show -> HintContent(viewModel, state, modifier)
    }
}

@Composable
private fun HintContent(
    viewModel: FilterHintViewModel,
    state: FilterHintViewState.Show,
    modifier: Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Popup(
        popupPositionProvider = remember { HintPopupPositionProvider() },
        onDismissRequest = viewModel::onCloseRequest,
        properties = PopupProperties(),
    ) {
        Card(modifier = Modifier.size(400.dp, 250.dp)) {
            LazyColumn {
                items(state.items, key = { it.text }) {
                    val modifier = if (it.key == state.selectedItemKey) {
                        Modifier.border(width = 1.dp, color = Color.Red)
                    } else {
                        Modifier
                    }
                    Text(it.text, modifier = modifier)
                }
            }
        }
    }
}

@Suppress("MagicNumber")
// TODO написать нормальный код вычисления местоположения
private class HintPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        return IntOffset(anchorBounds.left + 20, anchorBounds.top - 20 - popupContentSize.height)
    }
}
