package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val calendar_clock: ImageVector
    get() {
        if (_calendar_clock != null) {
            return _calendar_clock!!
        }
        _calendar_clock =
            ImageVector.Builder(
                name = "calendar_clock",
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
                        moveTo(5f, 8.81f)
                        horizontalLineTo(19f)
                        verticalLineTo(6.31f)
                        quadTo(19f, 6.19f, 18.9f, 6.1f)
                        reflectiveQuadTo(18.69f, 6f)
                        horizontalLineTo(5.31f)
                        quadTo(5.19f, 6f, 5.1f, 6.1f)
                        reflectiveQuadTo(5f, 6.31f)
                        verticalLineToRelative(2.5f)
                        close()
                        moveToRelative(0f, 0f)
                        verticalLineTo(6.31f)
                        quadTo(5f, 6.17f, 5f, 6.09f)
                        reflectiveQuadTo(5f, 6f)
                        quadTo(5f, 6f, 5f, 6.09f)
                        reflectiveQuadTo(5f, 6.31f)
                        verticalLineToRelative(2.5f)
                        close()
                        moveTo(5.31f, 21.5f)
                        quadToRelative(-0.76f, 0f, -1.28f, -0.52f)
                        reflectiveQuadTo(3.5f, 19.69f)
                        verticalLineTo(6.31f)
                        quadTo(3.5f, 5.55f, 4.03f, 5.03f)
                        reflectiveQuadTo(5.31f, 4.5f)
                        horizontalLineTo(6.69f)
                        verticalLineTo(2.38f)
                        horizontalLineTo(8.23f)
                        verticalLineTo(4.5f)
                        horizontalLineToRelative(7.58f)
                        verticalLineTo(2.38f)
                        horizontalLineToRelative(1.5f)
                        verticalLineTo(4.5f)
                        horizontalLineToRelative(1.38f)
                        quadToRelative(0.76f, 0f, 1.28f, 0.52f)
                        reflectiveQuadTo(20.5f, 6.31f)
                        verticalLineToRelative(5.46f)
                        quadTo(20.14f, 11.61f, 19.77f, 11.52f)
                        reflectiveQuadTo(19f, 11.36f)
                        verticalLineTo(10.31f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(9.38f)
                        quadToRelative(0f, 0.12f, 0.1f, 0.21f)
                        reflectiveQuadTo(5.31f, 20f)
                        horizontalLineToRelative(6.5f)
                        quadToRelative(0.13f, 0.42f, 0.31f, 0.79f)
                        reflectiveQuadToRelative(0.41f, 0.71f)
                        horizontalLineTo(5.31f)
                        close()
                        moveToRelative(9.7f, -0.31f)
                        quadTo(13.69f, 19.87f, 13.69f, 18f)
                        reflectiveQuadToRelative(1.31f, -3.19f)
                        reflectiveQuadTo(18.19f, 13.5f)
                        reflectiveQuadToRelative(3.19f, 1.31f)
                        reflectiveQuadTo(22.69f, 18f)
                        reflectiveQuadToRelative(-1.31f, 3.19f)
                        reflectiveQuadTo(18.19f, 22.5f)
                        reflectiveQuadTo(15.01f, 21.19f)
                        close()
                        moveToRelative(4.85f, -0.9f)
                        lineToRelative(0.62f, -0.62f)
                        lineTo(18.63f, 17.82f)
                        verticalLineTo(15.06f)
                        horizontalLineTo(17.75f)
                        verticalLineToRelative(3.12f)
                        lineToRelative(2.11f, 2.11f)
                        close()
                    }
                }
                .build()
        return _calendar_clock!!
    }

@Suppress("BackingPropertyNaming")
private var _calendar_clock: ImageVector? = null
