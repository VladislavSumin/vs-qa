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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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

private data class HighlightParams(
    val color: Color,
    val horizontalPadding: Float,
    val verticalPadding: Float,
    val cornerRadius: Float,
)

private fun HighlightParams.encode(): String = "${color.value}|$horizontalPadding|$verticalPadding|$cornerRadius"

private fun decodeHighlight(encoded: String): HighlightParams {
    val parts = encoded.split("|")
    return HighlightParams(
        color = Color(parts[0].toULong()),
        horizontalPadding = parts[1].toFloat(),
        verticalPadding = parts[2].toFloat(),
        cornerRadius = parts[3].toFloat(),
    )
}

fun AnnotatedString.Builder.highlightBackground(
    range: IntRange,
    background: Color,
    textColor: Color? = null,
    horizontalPadding: Dp = 0.dp,
    verticalPadding: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
) {
    if (textColor != null) {
        addStyle(SpanStyle(color = textColor), range.first, range.last + 1)
    }
    val params = HighlightParams(
        color = background,
        horizontalPadding = horizontalPadding.value,
        verticalPadding = verticalPadding.value,
        cornerRadius = cornerRadius.value,
    )
    addStringAnnotation(HIGHLIGHT_TAG, params.encode(), range.first, range.last + 1)
}

@Composable
fun HighlightedText(text: AnnotatedString, modifier: Modifier = Modifier, style: TextStyle = TextStyle.Default) {
    val density = LocalDensity.current

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicText(
        text = text,
        modifier = modifier.drawBehind {
            val layout = textLayoutResult ?: return@drawBehind
            val annotations = layout.layoutInput.text
                .getStringAnnotations(HIGHLIGHT_TAG, 0, layout.layoutInput.text.length)
            for (ann in annotations) {
                val params = decodeHighlight(ann.item)
                val hPadPx = with(density) { params.horizontalPadding.dp.toPx() }
                val vPadPx = with(density) { params.verticalPadding.dp.toPx() }
                val rPx = with(density) { params.cornerRadius.dp.toPx() }
                val rects = getRangeBoundsPerLine(layout, ann.start, ann.end)
                drawHighlightRects(rects, params.color, hPadPx, vPadPx, rPx)
            }
        },
        style = style,
        onTextLayout = { result -> textLayoutResult = result },
    )
}

private fun DrawScope.drawHighlightRects(
    rects: List<Rect>,
    color: Color,
    horizontalPaddingPx: Float,
    verticalPaddingPx: Float,
    cornerRadiusPx: Float,
) {
    if (rects.isEmpty()) return

    val padded = rects.map { rect ->
        Rect(
            left = rect.left - horizontalPaddingPx,
            top = rect.top - verticalPaddingPx,
            right = rect.right + horizontalPaddingPx,
            bottom = rect.bottom + verticalPaddingPx,
        )
    }

    val groups = splitIntoOverlappingGroups(padded)

    for (group in groups) {
        if (group.size == 1) {
            val r = group.first()
            drawRoundRect(
                color,
                Offset(r.left, r.top),
                Size(r.width, r.height),
                CornerRadius(cornerRadiusPx, cornerRadiusPx),
            )
        } else if (cornerRadiusPx <= 0f) {
            for (pr in group) {
                drawRect(color, Offset(pr.left, pr.top), Size(pr.width, pr.height))
            }
        } else {
            val path = buildHighlightPath(group, cornerRadiusPx)
            drawPath(path, color)
        }
    }
}

private fun splitIntoOverlappingGroups(rects: List<Rect>): List<List<Rect>> {
    val groups = mutableListOf<List<Rect>>()
    var current = mutableListOf<Rect>()
    for (rect in rects) {
        if (current.isEmpty()) {
            current.add(rect)
        } else {
            val prev = current.last()
            if (max(rect.left, prev.left) <= min(rect.right, prev.right)) {
                current.add(rect)
            } else {
                groups.add(current.toList())
                current = mutableListOf(rect)
            }
        }
    }
    if (current.isNotEmpty()) groups.add(current.toList())
    return groups
}

@Suppress("MagicNumber")
private fun buildHighlightPath(rects: List<Rect>, r: Float): Path {
    val first = rects.first()
    val last = rects.last()

    return Path().apply {
        moveTo(first.left, first.top + r)
        arcToCW(Rect(first.left, first.top, first.left + 2 * r, first.top + 2 * r), 180f)

        lineTo(first.right - r, first.top)
        arcToCW(Rect(first.right - 2 * r, first.top, first.right, first.top + 2 * r), 270f)

        for (i in 0 until rects.size - 1) {
            val curr = rects[i]
            val next = rects[i + 1]

            if (next.right > curr.right) {
                lineTo(curr.right, next.top - r)
                arcToCCW(Rect(curr.right, next.top - 2 * r, curr.right + 2 * r, next.top), 180f)
                lineTo(next.right - r, next.top)
                arcToCW(Rect(next.right - 2 * r, next.top, next.right, next.top + 2 * r), 270f)
            } else if (next.right < curr.right) {
                lineTo(curr.right, next.top - r)
                arcToCW(Rect(curr.right - 2 * r, next.top - 2 * r, curr.right, next.top), 0f)
                lineTo(next.right + r, next.top)
                arcToCCW(Rect(next.right, next.top, next.right + 2 * r, next.top + 2 * r), 270f)
            }
        }

        lineTo(last.right, last.bottom - r)
        arcToCW(Rect(last.right - 2 * r, last.bottom - 2 * r, last.right, last.bottom), 0f)

        lineTo(last.left + r, last.bottom)
        arcToCW(Rect(last.left, last.bottom - 2 * r, last.left + 2 * r, last.bottom), 90f)

        for (i in rects.size - 1 downTo 1) {
            val curr = rects[i]
            val prev = rects[i - 1]

            if (prev.left > curr.left) {
                lineTo(curr.left, prev.bottom + r)
                arcToCW(Rect(curr.left, prev.bottom, curr.left + 2 * r, prev.bottom + 2 * r), 180f)
                lineTo(prev.left - r, prev.bottom)
                arcToCCW(Rect(prev.left - 2 * r, prev.bottom - 2 * r, prev.left, prev.bottom), 90f)
            } else if (prev.left < curr.left) {
                lineTo(curr.left, prev.bottom + r)
                arcToCCW(Rect(curr.left - 2 * r, prev.bottom, curr.left, prev.bottom + 2 * r), 0f)
                lineTo(prev.left + r, prev.bottom)
                arcToCW(Rect(prev.left, prev.bottom - 2 * r, prev.left + 2 * r, prev.bottom), 90f)
            }
        }

        lineTo(first.left, first.top + r)
    }
}

@Suppress("MagicNumber")
private fun Path.arcToCW(rect: Rect, startAngle: Float) {
    arcTo(rect, startAngle, 90f, false)
}

@Suppress("MagicNumber")
private fun Path.arcToCCW(rect: Rect, startAngle: Float) {
    arcTo(rect, startAngle, -90f, false)
}

@Suppress("MagicNumber")
private fun getRangeBoundsPerLine(layout: TextLayoutResult, start: Int, end: Int): List<Rect> {
    if (end <= start) return emptyList()
    val result = mutableListOf<Rect>()
    var currentLine = -1
    var lineRect: Rect? = null
    for (offset in start until end) {
        val line = layout.getLineForOffset(offset)
        if (line != currentLine) {
            lineRect?.let { result.add(it) }
            currentLine = line
            lineRect = null
        }
        val charRect = layout.getBoundingBox(offset)
        val isZero = charRect.left == 0f && charRect.top == 0f &&
            charRect.right == 0f && charRect.bottom == 0f
        if (isZero) continue
        val current = lineRect
        lineRect = if (current != null) {
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
    lineRect?.let { result.add(it) }
    return result
}
