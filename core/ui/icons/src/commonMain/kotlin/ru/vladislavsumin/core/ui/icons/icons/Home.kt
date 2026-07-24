package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val home: ImageVector
    get() {
        if (_home != null) {
            return _home!!
        }
        _home =
            ImageVector.Builder(
                name = "home",
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
                        moveTo(6f, 19f)
                        horizontalLineTo(9.35f)
                        verticalLineTo(13.06f)
                        horizontalLineToRelative(5.31f)
                        verticalLineTo(19f)
                        horizontalLineTo(18f)
                        verticalLineTo(10f)
                        lineTo(12f, 5.48f)
                        lineTo(6f, 10f)
                        verticalLineToRelative(9f)
                        close()
                        moveTo(4.5f, 20.5f)
                        verticalLineTo(9.25f)
                        lineTo(12f, 3.61f)
                        lineToRelative(7.5f, 5.64f)
                        verticalLineTo(20.5f)
                        horizontalLineTo(13.15f)
                        verticalLineTo(14.56f)
                        horizontalLineTo(10.85f)
                        verticalLineTo(20.5f)
                        horizontalLineTo(4.5f)
                        close()
                        moveTo(12f, 12.24f)
                        close()
                    }
                }
                .build()
        return _home!!
    }

@Suppress("BackingPropertyNaming")
private var _home: ImageVector? = null
