package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val open_in_new: ImageVector
    get() {
        if (_open_in_new != null) {
            return _open_in_new!!
        }
        _open_in_new =
            ImageVector.Builder(
                name = "open_in_new",
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
                        moveTo(5.31f, 20.5f)
                        quadToRelative(-0.76f, 0f, -1.28f, -0.52f)
                        reflectiveQuadTo(3.5f, 18.69f)
                        verticalLineTo(5.31f)
                        quadTo(3.5f, 4.55f, 4.03f, 4.03f)
                        reflectiveQuadTo(5.31f, 3.5f)
                        horizontalLineToRelative(6.31f)
                        verticalLineTo(5f)
                        horizontalLineTo(5.31f)
                        quadTo(5.19f, 5f, 5.1f, 5.1f)
                        reflectiveQuadTo(5f, 5.31f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.12f, 0.1f, 0.21f)
                        reflectiveQuadTo(5.31f, 19f)
                        horizontalLineTo(18.69f)
                        quadToRelative(0.12f, 0f, 0.21f, -0.1f)
                        reflectiveQuadTo(19f, 18.69f)
                        verticalLineTo(12.38f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(6.31f)
                        quadToRelative(0f, 0.76f, -0.52f, 1.28f)
                        reflectiveQuadTo(18.69f, 20.5f)
                        horizontalLineTo(5.31f)
                        close()
                        moveTo(9.72f, 15.33f)
                        lineTo(8.67f, 14.28f)
                        lineTo(17.95f, 5f)
                        horizontalLineTo(14f)
                        verticalLineTo(3.5f)
                        horizontalLineToRelative(6.5f)
                        verticalLineTo(10f)
                        horizontalLineTo(19f)
                        verticalLineTo(6.05f)
                        lineTo(9.72f, 15.33f)
                        close()
                    }
                }
                .build()
        return _open_in_new!!
    }

@Suppress("BackingPropertyNaming")
private var _open_in_new: ImageVector? = null
