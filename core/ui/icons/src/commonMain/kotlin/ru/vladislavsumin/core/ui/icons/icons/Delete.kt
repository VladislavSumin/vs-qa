package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val delete: ImageVector
    get() {
        if (_delete != null) {
            return _delete!!
        }
        _delete =
            ImageVector.Builder(
                name = "delete",
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
                        moveTo(7.31f, 20.5f)
                        quadToRelative(-0.75f, 0f, -1.28f, -0.53f)
                        reflectiveQuadTo(5.5f, 18.69f)
                        verticalLineTo(6f)
                        horizontalLineToRelative(-1f)
                        verticalLineTo(4.5f)
                        horizontalLineTo(9f)
                        verticalLineTo(3.62f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(4.5f)
                        horizontalLineToRelative(4.5f)
                        verticalLineTo(6f)
                        horizontalLineToRelative(-1f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.76f, -0.52f, 1.28f)
                        reflectiveQuadTo(16.69f, 20.5f)
                        horizontalLineTo(7.31f)
                        close()
                        moveTo(17f, 6f)
                        horizontalLineTo(7f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.13f, 0.09f, 0.22f)
                        reflectiveQuadTo(7.31f, 19f)
                        horizontalLineToRelative(9.38f)
                        quadToRelative(0.12f, 0f, 0.21f, -0.1f)
                        reflectiveQuadTo(17f, 18.69f)
                        verticalLineTo(6f)
                        close()
                        moveTo(9.4f, 17f)
                        horizontalLineToRelative(1.5f)
                        verticalLineTo(8f)
                        horizontalLineTo(9.4f)
                        verticalLineToRelative(9f)
                        close()
                        moveToRelative(3.69f, 0f)
                        horizontalLineToRelative(1.5f)
                        verticalLineTo(8f)
                        horizontalLineTo(13.1f)
                        verticalLineToRelative(9f)
                        close()
                        moveTo(7f, 6f)
                        verticalLineTo(18.69f)
                        quadToRelative(0f, 0.13f, 0f, 0.22f)
                        reflectiveQuadTo(7f, 19f)
                        quadToRelative(0f, 0f, 0f, -0.09f)
                        reflectiveQuadTo(7f, 18.69f)
                        verticalLineTo(6f)
                        close()
                    }
                }
                .build()
        return _delete!!
    }

@Suppress("BackingPropertyNaming")
private var _delete: ImageVector? = null
