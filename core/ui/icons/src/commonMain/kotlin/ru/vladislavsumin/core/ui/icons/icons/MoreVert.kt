package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val more_vert: ImageVector
    get() {
        if (_more_vert != null) {
            return _more_vert!!
        }
        _more_vert =
            ImageVector.Builder(
                name = "more_vert",
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
                        moveTo(12f, 19.27f)
                        quadToRelative(-0.62f, 0f, -1.06f, -0.44f)
                        reflectiveQuadTo(10.5f, 17.77f)
                        reflectiveQuadToRelative(0.44f, -1.06f)
                        reflectiveQuadTo(12f, 16.27f)
                        reflectiveQuadToRelative(1.06f, 0.44f)
                        reflectiveQuadToRelative(0.44f, 1.06f)
                        reflectiveQuadToRelative(-0.44f, 1.06f)
                        reflectiveQuadTo(12f, 19.27f)
                        close()
                        moveTo(12f, 13.5f)
                        quadToRelative(-0.62f, 0f, -1.06f, -0.44f)
                        reflectiveQuadTo(10.5f, 12f)
                        reflectiveQuadToRelative(0.44f, -1.06f)
                        reflectiveQuadTo(12f, 10.5f)
                        reflectiveQuadToRelative(1.06f, 0.44f)
                        reflectiveQuadTo(13.5f, 12f)
                        reflectiveQuadToRelative(-0.44f, 1.06f)
                        reflectiveQuadTo(12f, 13.5f)
                        close()
                        moveTo(12f, 7.73f)
                        quadToRelative(-0.62f, 0f, -1.06f, -0.44f)
                        reflectiveQuadTo(10.5f, 6.23f)
                        reflectiveQuadTo(10.94f, 5.17f)
                        reflectiveQuadTo(12f, 4.73f)
                        reflectiveQuadToRelative(1.06f, 0.44f)
                        reflectiveQuadTo(13.5f, 6.23f)
                        reflectiveQuadTo(13.06f, 7.29f)
                        reflectiveQuadTo(12f, 7.73f)
                        close()
                    }
                }
                .build()
        return _more_vert!!
    }

@Suppress("BackingPropertyNaming")
private var _more_vert: ImageVector? = null
