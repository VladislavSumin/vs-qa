package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val save: ImageVector
    get() {
        if (_save != null) {
            return _save!!
        }
        _save =
            ImageVector.Builder(
                name = "save",
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
                        moveTo(20.5f, 7.21f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.76f, -0.52f, 1.28f)
                        reflectiveQuadTo(18.69f, 20.5f)
                        horizontalLineTo(5.31f)
                        quadToRelative(-0.76f, 0f, -1.28f, -0.52f)
                        reflectiveQuadTo(3.5f, 18.69f)
                        verticalLineTo(5.31f)
                        quadTo(3.5f, 4.55f, 4.03f, 4.03f)
                        reflectiveQuadTo(5.31f, 3.5f)
                        horizontalLineTo(16.79f)
                        lineTo(20.5f, 7.21f)
                        close()
                        moveTo(19f, 7.85f)
                        lineTo(16.15f, 5f)
                        horizontalLineTo(5.31f)
                        quadTo(5.17f, 5f, 5.09f, 5.09f)
                        reflectiveQuadTo(5f, 5.31f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.13f, 0.09f, 0.22f)
                        reflectiveQuadTo(5.31f, 19f)
                        horizontalLineTo(18.69f)
                        quadToRelative(0.13f, 0f, 0.22f, -0.09f)
                        reflectiveQuadTo(19f, 18.69f)
                        verticalLineTo(7.85f)
                        close()
                        moveToRelative(-5.23f, 8.69f)
                        quadTo(14.5f, 15.81f, 14.5f, 14.77f)
                        reflectiveQuadTo(13.77f, 13f)
                        reflectiveQuadTo(12f, 12.27f)
                        reflectiveQuadTo(10.23f, 13f)
                        reflectiveQuadTo(9.5f, 14.77f)
                        reflectiveQuadToRelative(0.73f, 1.77f)
                        reflectiveQuadTo(12f, 17.27f)
                        reflectiveQuadToRelative(1.77f, -0.73f)
                        close()
                        moveTo(6.38f, 9.88f)
                        horizontalLineTo(14.6f)
                        verticalLineTo(6.38f)
                        horizontalLineTo(6.38f)
                        verticalLineToRelative(3.5f)
                        close()
                        moveTo(5f, 7.85f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.13f, 0f, 0.22f)
                        reflectiveQuadTo(5f, 19f)
                        quadToRelative(0f, 0f, 0f, -0.09f)
                        reflectiveQuadTo(5f, 18.69f)
                        verticalLineTo(5.31f)
                        quadTo(5f, 5.17f, 5f, 5.09f)
                        reflectiveQuadTo(5f, 5f)
                        verticalLineTo(7.85f)
                        close()
                    }
                }
                .build()
        return _save!!
    }

@Suppress("BackingPropertyNaming")
private var _save: ImageVector? = null
