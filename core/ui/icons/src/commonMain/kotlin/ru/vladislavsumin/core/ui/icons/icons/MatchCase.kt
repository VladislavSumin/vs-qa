package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val match_case: ImageVector
    get() {
        if (_match_case != null) {
            return _match_case!!
        }
        _match_case =
            ImageVector.Builder(
                name = "match_case",
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
                        moveTo(3.67f, 17.43f)
                        lineTo(7.61f, 6.82f)
                        horizontalLineTo(9.14f)
                        lineToRelative(3.97f, 10.62f)
                        horizontalLineTo(11.61f)
                        lineToRelative(-1f, -2.88f)
                        horizontalLineTo(6.17f)
                        lineTo(5.16f, 17.43f)
                        horizontalLineTo(3.67f)
                        close()
                        moveTo(6.62f, 13.3f)
                        horizontalLineToRelative(3.52f)
                        lineTo(8.42f, 8.41f)
                        horizontalLineTo(8.33f)
                        lineTo(6.62f, 13.3f)
                        close()
                        moveToRelative(9.76f, 4.37f)
                        quadToRelative(-1.22f, 0f, -1.94f, -0.66f)
                        reflectiveQuadTo(13.72f, 15.3f)
                        quadToRelative(0f, -1.08f, 0.85f, -1.76f)
                        reflectiveQuadToRelative(2.18f, -0.68f)
                        quadToRelative(0.56f, 0f, 1.11f, 0.1f)
                        reflectiveQuadToRelative(0.96f, 0.29f)
                        verticalLineTo(12.81f)
                        quadToRelative(0f, -0.85f, -0.5f, -1.33f)
                        reflectiveQuadTo(16.9f, 11f)
                        quadToRelative(-0.55f, 0f, -1.01f, 0.21f)
                        reflectiveQuadToRelative(-0.86f, 0.62f)
                        lineTo(14.13f, 11.11f)
                        quadToRelative(0.58f, -0.62f, 1.26f, -0.91f)
                        reflectiveQuadTo(16.92f, 9.9f)
                        quadToRelative(1.58f, 0f, 2.35f, 0.77f)
                        reflectiveQuadTo(20.05f, 13f)
                        verticalLineToRelative(4.43f)
                        horizontalLineTo(18.81f)
                        verticalLineTo(16.46f)
                        horizontalLineToRelative(-0.1f)
                        quadToRelative(-0.37f, 0.6f, -0.96f, 0.91f)
                        reflectiveQuadToRelative(-1.36f, 0.3f)
                        close()
                        moveToRelative(0.18f, -1.08f)
                        quadToRelative(1.01f, 0f, 1.63f, -0.67f)
                        reflectiveQuadTo(18.82f, 14.3f)
                        quadTo(18.47f, 14.1f, 17.96f, 13.99f)
                        reflectiveQuadTo(16.96f, 13.88f)
                        quadToRelative(-0.9f, 0f, -1.41f, 0.38f)
                        reflectiveQuadTo(15.02f, 15.3f)
                        quadToRelative(0f, 0.56f, 0.43f, 0.93f)
                        reflectiveQuadToRelative(1.11f, 0.37f)
                        close()
                    }
                }
                .build()
        return _match_case!!
    }

@Suppress("BackingPropertyNaming")
private var _match_case: ImageVector? = null
