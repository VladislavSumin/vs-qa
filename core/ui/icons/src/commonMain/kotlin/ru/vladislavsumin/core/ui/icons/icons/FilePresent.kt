package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val file_present: ImageVector
    get() {
        if (_file_present != null) {
            return _file_present!!
        }
        _file_present =
            ImageVector.Builder(
                name = "file_present",
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
                        moveTo(6.31f, 21.5f)
                        quadToRelative(-0.76f, 0f, -1.28f, -0.52f)
                        reflectiveQuadTo(4.5f, 19.69f)
                        verticalLineTo(4.31f)
                        quadTo(4.5f, 3.55f, 5.03f, 3.03f)
                        reflectiveQuadTo(6.31f, 2.5f)
                        horizontalLineToRelative(8.56f)
                        lineTo(19.5f, 7.13f)
                        verticalLineTo(19.69f)
                        quadToRelative(0f, 0.76f, -0.52f, 1.28f)
                        reflectiveQuadTo(17.69f, 21.5f)
                        horizontalLineTo(6.31f)
                        close()
                        moveToRelative(0f, -1.5f)
                        horizontalLineTo(17.69f)
                        quadToRelative(0.12f, 0f, 0.21f, -0.1f)
                        reflectiveQuadTo(18f, 19.69f)
                        verticalLineTo(7.88f)
                        horizontalLineTo(14.12f)
                        verticalLineTo(4f)
                        horizontalLineTo(6.31f)
                        quadTo(6.19f, 4f, 6.1f, 4.1f)
                        reflectiveQuadTo(6f, 4.31f)
                        verticalLineTo(19.69f)
                        quadToRelative(0f, 0.12f, 0.1f, 0.21f)
                        reflectiveQuadTo(6.31f, 20f)
                        close()
                        moveTo(12f, 18.61f)
                        quadToRelative(1.52f, 0f, 2.57f, -1.09f)
                        reflectiveQuadTo(15.62f, 14.9f)
                        verticalLineTo(10.62f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineTo(14.9f)
                        quadToRelative(0f, 0.9f, -0.61f, 1.55f)
                        reflectiveQuadTo(12f, 17.11f)
                        quadToRelative(-0.88f, 0f, -1.5f, -0.65f)
                        reflectiveQuadTo(9.88f, 14.9f)
                        verticalLineTo(9.42f)
                        quadToRelative(0f, -0.31f, 0.2f, -0.53f)
                        reflectiveQuadTo(10.57f, 8.68f)
                        quadToRelative(0.3f, 0f, 0.49f, 0.21f)
                        reflectiveQuadToRelative(0.19f, 0.53f)
                        verticalLineTo(14.9f)
                        horizontalLineToRelative(1.5f)
                        verticalLineTo(9.42f)
                        quadToRelative(0f, -0.92f, -0.63f, -1.58f)
                        reflectiveQuadTo(10.57f, 7.18f)
                        reflectiveQuadTo(9.01f, 7.84f)
                        reflectiveQuadTo(8.38f, 9.42f)
                        verticalLineTo(14.9f)
                        quadToRelative(0f, 1.52f, 1.05f, 2.61f)
                        reflectiveQuadTo(12f, 18.61f)
                        close()
                        moveTo(6f, 4f)
                        verticalLineTo(7.88f)
                        verticalLineTo(4f)
                        verticalLineTo(7.88f)
                        verticalLineTo(19.69f)
                        quadToRelative(0f, 0.12f, 0f, 0.21f)
                        reflectiveQuadTo(6f, 20f)
                        quadToRelative(0f, 0f, 0f, -0.1f)
                        reflectiveQuadTo(6f, 19.69f)
                        verticalLineTo(4.31f)
                        quadTo(6f, 4.19f, 6f, 4.1f)
                        reflectiveQuadTo(6f, 4f)
                        close()
                    }
                }
                .build()
        return _file_present!!
    }

@Suppress("BackingPropertyNaming")
private var _file_present: ImageVector? = null
