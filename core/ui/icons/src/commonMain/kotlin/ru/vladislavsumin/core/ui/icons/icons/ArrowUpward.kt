package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val arrow_upward: ImageVector
    get() {
        if (_arrow_upward != null) {
            return _arrow_upward!!
        }
        _arrow_upward =
            ImageVector.Builder(
                name = "arrow_upward",
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
                        moveTo(11.25f, 19.5f)
                        verticalLineTo(7.37f)
                        lineToRelative(-5.7f, 5.7f)
                        lineTo(4.5f, 12f)
                        lineTo(12f, 4.5f)
                        lineTo(19.5f, 12f)
                        lineToRelative(-1.05f, 1.07f)
                        lineToRelative(-5.7f, -5.7f)
                        verticalLineTo(19.5f)
                        horizontalLineToRelative(-1.5f)
                        close()
                    }
                }
                .build()
        return _arrow_upward!!
    }

@Suppress("BackingPropertyNaming")
private var _arrow_upward: ImageVector? = null
