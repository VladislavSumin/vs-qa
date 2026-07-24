package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val arrow_downward: ImageVector
    get() {
        if (_arrow_downward != null) {
            return _arrow_downward!!
        }
        _arrow_downward =
            ImageVector.Builder(
                name = "arrow_downward",
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
                        moveTo(11.25f, 4.5f)
                        verticalLineTo(16.63f)
                        lineToRelative(-5.7f, -5.7f)
                        lineTo(4.5f, 12f)
                        lineTo(12f, 19.5f)
                        lineTo(19.5f, 12f)
                        lineTo(18.45f, 10.93f)
                        lineToRelative(-5.7f, 5.7f)
                        verticalLineTo(4.5f)
                        horizontalLineToRelative(-1.5f)
                        close()
                    }
                }
                .build()
        return _arrow_downward!!
    }

@Suppress("BackingPropertyNaming")
private var _arrow_downward: ImageVector? = null
