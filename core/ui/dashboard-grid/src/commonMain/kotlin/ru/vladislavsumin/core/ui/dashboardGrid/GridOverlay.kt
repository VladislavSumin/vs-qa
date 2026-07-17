package ru.vladislavsumin.core.ui.dashboardGrid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

@Composable
internal fun GridOverlay(columns: Int, rows: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val color = Color.Gray.copy(alpha = 0.15f)
        val cellW = size.width / columns
        val cellH = size.height / rows

        for (i in 1 until columns) {
            drawLine(
                color = color,
                start = Offset(i * cellW, 0f),
                end = Offset(i * cellW, size.height),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
            )
        }
        for (i in 1 until rows) {
            drawLine(
                color = color,
                start = Offset(0f, i * cellH),
                end = Offset(size.width, i * cellH),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
            )
        }
    }
}
