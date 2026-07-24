package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val close: ImageVector
    get() {
        if (_close != null) {
            return _close!!
        }
        _close =
            ImageVector.Builder(
                name = "close",
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
                        moveTo(6.4f, 18.65f)
                        lineTo(5.35f, 17.6f)
                        lineTo(10.95f, 12f)
                        lineTo(5.35f, 6.4f)
                        lineTo(6.4f, 5.35f)
                        lineToRelative(5.6f, 5.6f)
                        lineToRelative(5.6f, -5.6f)
                        lineTo(18.65f, 6.4f)
                        lineTo(13.05f, 12f)
                        lineToRelative(5.6f, 5.6f)
                        lineTo(17.6f, 18.65f)
                        lineTo(12f, 13.05f)
                        lineToRelative(-5.6f, 5.6f)
                        close()
                    }
                }
                .build()
        return _close!!
    }

@Suppress("BackingPropertyNaming")
private var _close: ImageVector? = null
