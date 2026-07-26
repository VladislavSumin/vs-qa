package ru.vladislavsumin.core.ui.textHighlight

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

private const val HIGHLIGHT_TAG = "core.ui.text-highlight"

private fun Color.encode(): String = value.toString()

private fun decode(encoded: String): Color = Color(encoded.toULong())

fun AnnotatedString.Builder.highlightBackground(range: IntRange, background: Color, textColor: Color? = null) {
    if (textColor != null) {
        addStyle(SpanStyle(color = textColor), range.first, range.last + 1)
    }
    addStringAnnotation(HIGHLIGHT_TAG, background.encode(), range.first, range.last + 1)
}

@Composable
fun HighlightedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    horizontalPadding: Dp = 2.dp,
    verticalPadding: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
) {
    val density = LocalDensity.current
    val horizontalPaddingPx = with(density) { horizontalPadding.toPx() }
    val verticalPaddingPx = with(density) { verticalPadding.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicText(
        text = text,
        modifier = modifier.drawBehind {
            val layout = textLayoutResult ?: return@drawBehind
            val annotations = layout.layoutInput.text
                .getStringAnnotations(HIGHLIGHT_TAG, 0, layout.layoutInput.text.length)
            for (ann in annotations) {
                val bgColor = decode(ann.item)
                val rect = getRangeBounds(layout, ann.start, ann.end) ?: continue
                drawRoundRect(
                    color = bgColor,
                    topLeft = Offset(rect.left - horizontalPaddingPx, rect.top - verticalPaddingPx),
                    size = Size(
                        rect.width + 2 * horizontalPaddingPx,
                        rect.height + 2 * verticalPaddingPx,
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                )
            }
        },
        style = style,
        onTextLayout = { result -> textLayoutResult = result },
    )
}

@Suppress("MagicNumber")
private fun getRangeBounds(layout: TextLayoutResult, start: Int, end: Int): Rect? {
    if (end <= start) return null
    var result: Rect? = null
    for (offset in start until end) {
        val charRect = layout.getBoundingBox(offset)
        val isZero = charRect.left == 0f && charRect.top == 0f &&
            charRect.right == 0f && charRect.bottom == 0f
        if (isZero) continue
        val current = result
        result = if (current != null) {
            Rect(
                left = min(current.left, charRect.left),
                top = min(current.top, charRect.top),
                right = max(current.right, charRect.right),
                bottom = max(current.bottom, charRect.bottom),
            )
        } else {
            charRect
        }
    }
    return result
}
