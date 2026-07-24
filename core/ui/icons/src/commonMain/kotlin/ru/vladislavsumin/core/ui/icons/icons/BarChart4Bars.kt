package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val bar_chart_4_bars: ImageVector
    get() {
        if (_bar_chart_4_bars != null) {
            return _bar_chart_4_bars!!
        }
        _bar_chart_4_bars =
            ImageVector.Builder(
                name = "bar_chart_4_bars",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(2.5f, 20.5f)
                        verticalLineTo(19f)
                        horizontalLineToRelative(19f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(2.5f)
                        close()
                        moveToRelative(1f, -2.88f)
                        verticalLineTo(11.5f)
                        horizontalLineTo(6f)
                        verticalLineToRelative(6.12f)
                        horizontalLineTo(3.5f)
                        close()
                        moveToRelative(4.83f, 0f)
                        verticalLineTo(6.5f)
                        horizontalLineToRelative(2.5f)
                        verticalLineTo(17.62f)
                        horizontalLineTo(8.33f)
                        close()
                        moveToRelative(4.84f, 0f)
                        verticalLineTo(9.5f)
                        horizontalLineToRelative(2.5f)
                        verticalLineToRelative(8.12f)
                        horizontalLineToRelative(-2.5f)
                        close()
                        moveToRelative(4.84f, 0f)
                        verticalLineTo(3.5f)
                        horizontalLineToRelative(2.5f)
                        verticalLineTo(17.62f)
                        horizontalLineTo(18f)
                        close()
                    }
                }
                .build()
        return _bar_chart_4_bars!!
    }

@Suppress("BackingPropertyNaming")
private var _bar_chart_4_bars: ImageVector? = null
