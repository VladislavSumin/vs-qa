package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val content_copy: ImageVector
    get() {
        if (_content_copy != null) {
            return _content_copy!!
        }
        _content_copy =
            ImageVector.Builder(
                name = "content_copy",
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
                        moveTo(9.06f, 17.5f)
                        quadTo(8.3f, 17.5f, 7.78f, 16.97f)
                        reflectiveQuadTo(7.25f, 15.69f)
                        verticalLineTo(4.31f)
                        quadToRelative(0f, -0.76f, 0.52f, -1.28f)
                        reflectiveQuadTo(9.06f, 2.5f)
                        horizontalLineToRelative(8.38f)
                        quadToRelative(0.76f, 0f, 1.28f, 0.52f)
                        reflectiveQuadToRelative(0.52f, 1.28f)
                        verticalLineTo(15.69f)
                        quadToRelative(0f, 0.76f, -0.52f, 1.28f)
                        reflectiveQuadTo(17.44f, 17.5f)
                        horizontalLineTo(9.06f)
                        close()
                        moveToRelative(0f, -1.5f)
                        horizontalLineToRelative(8.38f)
                        quadToRelative(0.12f, 0f, 0.21f, -0.1f)
                        reflectiveQuadToRelative(0.1f, -0.21f)
                        verticalLineTo(4.31f)
                        quadToRelative(0f, -0.12f, -0.1f, -0.21f)
                        reflectiveQuadTo(17.44f, 4f)
                        horizontalLineTo(9.06f)
                        quadTo(8.94f, 4f, 8.85f, 4.1f)
                        reflectiveQuadTo(8.75f, 4.31f)
                        verticalLineTo(15.69f)
                        quadToRelative(0f, 0.12f, 0.1f, 0.21f)
                        reflectiveQuadTo(9.06f, 16f)
                        close()
                        moveToRelative(-3.5f, 5f)
                        quadTo(4.8f, 21f, 4.28f, 20.47f)
                        reflectiveQuadTo(3.75f, 19.19f)
                        verticalLineTo(6.31f)
                        horizontalLineToRelative(1.5f)
                        verticalLineTo(19.19f)
                        quadToRelative(0f, 0.12f, 0.1f, 0.21f)
                        reflectiveQuadToRelative(0.21f, 0.1f)
                        horizontalLineToRelative(9.88f)
                        verticalLineTo(21f)
                        horizontalLineTo(5.56f)
                        close()
                        moveTo(8.75f, 16f)
                        quadToRelative(0f, 0f, 0f, -0.1f)
                        reflectiveQuadToRelative(0f, -0.21f)
                        verticalLineTo(4.31f)
                        quadToRelative(0f, -0.12f, 0f, -0.21f)
                        reflectiveQuadTo(8.75f, 4f)
                        quadToRelative(0f, 0f, 0f, 0.1f)
                        reflectiveQuadToRelative(0f, 0.21f)
                        verticalLineTo(15.69f)
                        quadToRelative(0f, 0.12f, 0f, 0.21f)
                        reflectiveQuadToRelative(0f, 0.1f)
                        close()
                    }
                }
                .build()
        return _content_copy!!
    }

@Suppress("BackingPropertyNaming")
private var _content_copy: ImageVector? = null
