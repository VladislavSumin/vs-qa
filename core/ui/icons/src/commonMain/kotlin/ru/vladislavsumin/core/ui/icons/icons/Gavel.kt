package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val gavel: ImageVector
    get() {
        if (_gavel != null) {
            return _gavel!!
        }
        _gavel =
            ImageVector.Builder(
                name = "gavel",
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
                        moveTo(4.06f, 20.75f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineToRelative(11.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(4.06f)
                        close()
                        moveToRelative(5.8f, -5.16f)
                        lineToRelative(-5.3f, -5.3f)
                        lineTo(6.31f, 8.48f)
                        lineToRelative(5.35f, 5.3f)
                        lineToRelative(-1.8f, 1.8f)
                        close()
                        moveTo(15.79f, 9.67f)
                        lineTo(10.48f, 4.31f)
                        lineToRelative(1.8f, -1.75f)
                        lineToRelative(5.3f, 5.3f)
                        lineToRelative(-1.8f, 1.8f)
                        close()
                        moveToRelative(4.81f, 9.99f)
                        lineTo(7.9f, 6.95f)
                        lineTo(8.95f, 5.9f)
                        lineToRelative(12.7f, 12.7f)
                        lineTo(20.6f, 19.65f)
                        close()
                    }
                }
                .build()
        return _gavel!!
    }

@Suppress("BackingPropertyNaming")
private var _gavel: ImageVector? = null
