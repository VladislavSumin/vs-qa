package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val bookmarks: ImageVector
    get() {
        if (_bookmarks != null) {
            return _bookmarks!!
        }
        _bookmarks =
            ImageVector.Builder(
                name = "bookmarks",
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
                        moveTo(4.5f, 21.5f)
                        verticalLineTo(8.23f)
                        quadTo(4.5f, 7.48f, 5.03f, 6.95f)
                        reflectiveQuadTo(6.31f, 6.42f)
                        horizontalLineToRelative(7.53f)
                        quadToRelative(0.75f, 0f, 1.28f, 0.53f)
                        reflectiveQuadToRelative(0.53f, 1.28f)
                        verticalLineTo(21.5f)
                        lineTo(10.08f, 18.67f)
                        lineTo(4.5f, 21.5f)
                        close()
                        moveTo(6f, 19.19f)
                        lineToRelative(4.08f, -2.16f)
                        lineToRelative(4.07f, 2.16f)
                        verticalLineTo(8.23f)
                        quadToRelative(0f, -0.13f, -0.09f, -0.22f)
                        reflectiveQuadTo(13.84f, 7.92f)
                        horizontalLineTo(6.31f)
                        quadToRelative(-0.13f, 0f, -0.22f, 0.09f)
                        reflectiveQuadTo(6f, 8.23f)
                        verticalLineTo(19.19f)
                        close()
                        moveTo(18f, 17.72f)
                        verticalLineTo(4.31f)
                        quadTo(18f, 4.17f, 17.91f, 4.09f)
                        reflectiveQuadTo(17.69f, 4f)
                        horizontalLineTo(7.39f)
                        verticalLineTo(2.5f)
                        horizontalLineToRelative(10.3f)
                        quadToRelative(0.75f, 0f, 1.28f, 0.53f)
                        reflectiveQuadTo(19.5f, 4.31f)
                        verticalLineTo(17.72f)
                        horizontalLineTo(18f)
                        close()
                        moveTo(6f, 7.92f)
                        quadToRelative(0f, 0f, 0.09f, 0f)
                        reflectiveQuadToRelative(0.22f, 0f)
                        horizontalLineToRelative(7.53f)
                        quadToRelative(0.13f, 0f, 0.22f, 0f)
                        reflectiveQuadToRelative(0.09f, 0f)
                        horizontalLineTo(10.08f)
                        horizontalLineTo(6f)
                        close()
                    }
                }
                .build()
        return _bookmarks!!
    }

@Suppress("BackingPropertyNaming")
private var _bookmarks: ImageVector? = null
