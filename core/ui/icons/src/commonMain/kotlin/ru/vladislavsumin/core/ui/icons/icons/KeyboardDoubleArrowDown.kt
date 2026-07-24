package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val keyboard_double_arrow_down: ImageVector
    get() {
        if (_keyboard_double_arrow_down != null) {
            return _keyboard_double_arrow_down!!
        }
        _keyboard_double_arrow_down =
            ImageVector.Builder(
                name = "keyboard_double_arrow_down",
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
                        moveTo(12f, 18.63f)
                        lineTo(6.35f, 12.98f)
                        lineTo(7.4f, 11.93f)
                        lineTo(12f, 16.51f)
                        lineToRelative(4.6f, -4.58f)
                        lineToRelative(1.05f, 1.05f)
                        lineTo(12f, 18.63f)
                        close()
                        moveToRelative(0f, -5.98f)
                        lineTo(6.35f, 7f)
                        lineTo(7.4f, 5.95f)
                        lineTo(12f, 10.53f)
                        lineTo(16.6f, 5.95f)
                        lineTo(17.65f, 7f)
                        lineTo(12f, 12.65f)
                        close()
                    }
                }
                .build()
        return _keyboard_double_arrow_down!!
    }

@Suppress("BackingPropertyNaming")
private var _keyboard_double_arrow_down: ImageVector? = null
