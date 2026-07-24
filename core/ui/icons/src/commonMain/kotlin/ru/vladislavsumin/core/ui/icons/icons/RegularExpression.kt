package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val regular_expression: ImageVector
    get() {
        if (_regular_expression != null) {
            return _regular_expression!!
        }
        _regular_expression =
            ImageVector.Builder(
                name = "regular_expression",
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
                        moveTo(5.34f, 18.74f)
                        quadTo(3.99f, 17.37f, 3.24f, 15.63f)
                        reflectiveQuadTo(2.49f, 11.96f)
                        reflectiveQuadTo(3.22f, 8.28f)
                        reflectiveQuadTo(5.34f, 5.17f)
                        lineTo(6.42f, 6.24f)
                        quadTo(5.24f, 7.37f, 4.61f, 8.85f)
                        reflectiveQuadToRelative(-0.62f, 3.1f)
                        reflectiveQuadToRelative(0.64f, 3.1f)
                        reflectiveQuadToRelative(1.79f, 2.62f)
                        lineTo(5.34f, 18.74f)
                        close()
                        moveTo(8.62f, 17.38f)
                        quadTo(8.25f, 17.02f, 8.25f, 16.5f)
                        reflectiveQuadTo(8.62f, 15.62f)
                        reflectiveQuadTo(9.5f, 15.25f)
                        reflectiveQuadToRelative(0.88f, 0.37f)
                        reflectiveQuadToRelative(0.37f, 0.88f)
                        reflectiveQuadToRelative(-0.37f, 0.88f)
                        reflectiveQuadTo(9.5f, 17.75f)
                        reflectiveQuadTo(8.62f, 17.38f)
                        close()
                        moveToRelative(4.61f, -4.63f)
                        verticalLineTo(10.78f)
                        lineToRelative(-1.69f, 1f)
                        lineTo(10.79f, 10.47f)
                        lineTo(12.48f, 9.5f)
                        lineTo(10.79f, 8.53f)
                        lineTo(11.54f, 7.22f)
                        lineToRelative(1.69f, 1f)
                        verticalLineTo(6.25f)
                        horizontalLineToRelative(1.5f)
                        verticalLineTo(8.22f)
                        lineToRelative(1.69f, -1f)
                        lineToRelative(0.75f, 1.31f)
                        lineTo(15.47f, 9.5f)
                        lineToRelative(1.69f, 0.97f)
                        lineToRelative(-0.75f, 1.31f)
                        lineToRelative(-1.69f, -1f)
                        verticalLineToRelative(1.97f)
                        horizontalLineToRelative(-1.5f)
                        close()
                        moveToRelative(5.43f, 5.99f)
                        lineTo(17.57f, 17.67f)
                        quadToRelative(1.18f, -1.13f, 1.8f, -2.61f)
                        reflectiveQuadTo(20f, 11.95f)
                        reflectiveQuadTo(19.36f, 8.85f)
                        reflectiveQuadTo(17.57f, 6.24f)
                        lineTo(18.65f, 5.17f)
                        quadTo(20f, 6.54f, 20.75f, 8.29f)
                        reflectiveQuadToRelative(0.75f, 3.67f)
                        reflectiveQuadToRelative(-0.74f, 3.68f)
                        reflectiveQuadToRelative(-2.11f, 3.1f)
                        close()
                    }
                }
                .build()
        return _regular_expression!!
    }

@Suppress("BackingPropertyNaming")
private var _regular_expression: ImageVector? = null
