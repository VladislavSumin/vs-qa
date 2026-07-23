package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

internal class TabShape(private val topCornerRadius: Dp = 4.dp, private val bottomOutwardRadius: Dp = 4.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val topR = with(density) { topCornerRadius.toPx() }
        val bottomR = with(density) { bottomOutwardRadius.toPx() }
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(topR, 0f)

            arcTo(
                rect = Rect(width - 2 * topR, 0f, width, 2 * topR),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            lineTo(width, height - bottomR)

            arcTo(
                rect = Rect(
                    width,
                    height - 2 * bottomR,
                    width + 2 * bottomR,
                    height,
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false,
            )

            lineTo(bottomR, height)

            arcTo(
                rect = Rect(
                    -2 * bottomR,
                    height - 2 * bottomR,
                    0f,
                    height,
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false,
            )

            lineTo(0f, topR)

            arcTo(
                rect = Rect(0f, 0f, 2 * topR, 2 * topR),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )

            close()
        }

        return Outline.Generic(path)
    }
}
