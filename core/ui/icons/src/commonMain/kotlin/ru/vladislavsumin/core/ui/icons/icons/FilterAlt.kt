package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val filter_alt: ImageVector
    get() {
        if (_filter_alt != null) {
            return _filter_alt!!
        }
        _filter_alt =
            ImageVector.Builder(
                name = "filter_alt",
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
                        moveTo(11.38f, 19.5f)
                        quadToRelative(-0.38f, 0f, -0.63f, -0.25f)
                        reflectiveQuadTo(10.5f, 18.62f)
                        verticalLineTo(12.83f)
                        lineTo(4.9f, 5.72f)
                        quadTo(4.61f, 5.33f, 4.82f, 4.92f)
                        reflectiveQuadTo(5.51f, 4.5f)
                        horizontalLineTo(18.49f)
                        quadToRelative(0.49f, 0f, 0.69f, 0.42f)
                        reflectiveQuadTo(19.1f, 5.72f)
                        lineToRelative(-5.6f, 7.11f)
                        verticalLineToRelative(5.79f)
                        quadToRelative(0f, 0.38f, -0.25f, 0.63f)
                        reflectiveQuadTo(12.62f, 19.5f)
                        horizontalLineTo(11.38f)
                        close()
                        moveTo(12f, 12.3f)
                        lineTo(16.95f, 6f)
                        horizontalLineTo(7.05f)
                        lineTo(12f, 12.3f)
                        close()
                        moveToRelative(0f, 0f)
                        close()
                    }
                }
                .build()
        return _filter_alt!!
    }

@Suppress("BackingPropertyNaming")
private var _filter_alt: ImageVector? = null
