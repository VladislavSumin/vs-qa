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
fun Modifier.hint(text: String, delayMillis: Long = 450L, placement: HintPlacement = HintPlacement.BOTTOM): Modifier {
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
            popupPositionProvider = remember { HintPopupPositionProvider(layoutCoordinates!!, placement) },
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

enum class HintPlacement { TOP, BOTTOM, LEFT, RIGHT }

private class HintPopupPositionProvider(
    private val elementCoordinates: LayoutCoordinates,
    private val preferredPlacement: HintPlacement,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val pos = elementCoordinates.positionInWindow()
        val size = elementCoordinates.size

        val elementLeft = pos.x.roundToInt()
        val elementRight = elementLeft + size.width
        val elementTop = pos.y.roundToInt()
        val elementBottom = elementTop + size.height
        val elementCenterX = elementLeft + size.width / 2
        val elementCenterY = elementTop + size.height / 2

        val cw = popupContentSize.width
        val ch = popupContentSize.height
        val gap = 4
        val ww = windowSize.width
        val wh = windowSize.height

        val placementPriority = buildPriority(preferredPlacement)

        for (p in placementPriority) {
            when (p) {
                HintPlacement.BOTTOM -> {
                    if (elementBottom + gap + ch <= wh) {
                        return IntOffset(
                            (elementCenterX - cw / 2).coerceIn(0, (ww - cw).coerceAtLeast(0)),
                            elementBottom + gap,
                        )
                    }
                }

                HintPlacement.TOP -> {
                    if (elementTop - gap - ch >= 0) {
                        return IntOffset(
                            (elementCenterX - cw / 2).coerceIn(0, (ww - cw).coerceAtLeast(0)),
                            elementTop - gap - ch,
                        )
                    }
                }

                HintPlacement.RIGHT -> {
                    if (elementRight + gap + cw <= ww) {
                        return IntOffset(
                            elementRight + gap,
                            (elementCenterY - ch / 2).coerceIn(0, (wh - ch).coerceAtLeast(0)),
                        )
                    }
                }

                HintPlacement.LEFT -> {
                    if (elementLeft - gap - cw >= 0) {
                        return IntOffset(
                            elementLeft - gap - cw,
                            (elementCenterY - ch / 2).coerceIn(0, (wh - ch).coerceAtLeast(0)),
                        )
                    }
                }
            }
        }

        return IntOffset(
            (elementCenterX - cw / 2).coerceIn(0, (ww - cw).coerceAtLeast(0)),
            (wh - ch).coerceAtLeast(0),
        )
    }

    private fun buildPriority(preferred: HintPlacement): List<HintPlacement> {
        val opposite = when (preferred) {
            HintPlacement.BOTTOM -> HintPlacement.TOP
            HintPlacement.TOP -> HintPlacement.BOTTOM
            HintPlacement.LEFT -> HintPlacement.RIGHT
            HintPlacement.RIGHT -> HintPlacement.LEFT
        }
        val horizontals = listOf(HintPlacement.LEFT, HintPlacement.RIGHT)
        val verticals = listOf(HintPlacement.TOP, HintPlacement.BOTTOM)
        val side = if (preferred in verticals) horizontals else verticals
        return listOf(preferred, opposite) + side
    }
}
