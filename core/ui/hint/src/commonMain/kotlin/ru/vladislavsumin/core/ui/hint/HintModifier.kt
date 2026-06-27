package ru.vladislavsumin.core.ui.hint

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun Modifier.hint(text: String, delayMillis: Long = 450L): Modifier {
    var isHovered by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(delayMillis)
            showHint = true
        } else {
            showHint = false
        }
    }

    if (showHint && layoutCoordinates != null) {
        Popup(
            popupPositionProvider = remember { HintPopupPositionProvider(layoutCoordinates!!) },
            onDismissRequest = { showHint = false },
            properties = PopupProperties(focusable = false),
        ) {
            Card(
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.widthIn(max = 300.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }

    return this
        .onGloballyPositioned { layoutCoordinates = it }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    when (awaitPointerEvent().type) {
                        PointerEventType.Enter -> isHovered = true
                        PointerEventType.Exit -> isHovered = false
                        else -> {}
                    }
                }
            }
        }
}

private class HintPopupPositionProvider(private val elementCoordinates: LayoutCoordinates) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val position = elementCoordinates.positionInWindow()
        val size = elementCoordinates.size

        val elementCenterX = position.x.roundToInt() + size.width / 2
        val elementTop = position.y.roundToInt()
        val elementBottom = position.y.roundToInt() + size.height

        val contentWidth = popupContentSize.width
        val contentHeight = popupContentSize.height
        val gap = 4

        val fitsBelow = elementBottom + gap + contentHeight <= windowSize.height
        val fitsAbove = elementTop - gap - contentHeight >= 0

        val y = when {
            fitsBelow -> elementBottom + gap
            fitsAbove -> elementTop - gap - contentHeight
            else -> windowSize.height - contentHeight
        }.coerceAtLeast(0)

        val x = (elementCenterX - contentWidth / 2)
            .coerceIn(0, windowSize.width - contentWidth)
            .coerceAtLeast(0)

        return IntOffset(x, y)
    }
}
