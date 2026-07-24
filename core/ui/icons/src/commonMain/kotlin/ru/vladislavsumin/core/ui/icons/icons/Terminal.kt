package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val terminal: ImageVector
    get() {
        if (_terminal != null) {
            return _terminal!!
        }
        _terminal =
            ImageVector.Builder(
                name = "terminal",
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
                        moveTo(4.31f, 19.5f)
                        quadToRelative(-0.76f, 0f, -1.28f, -0.52f)
                        reflectiveQuadTo(2.5f, 17.69f)
                        verticalLineTo(6.31f)
                        quadTo(2.5f, 5.55f, 3.03f, 5.03f)
                        reflectiveQuadTo(4.31f, 4.5f)
                        horizontalLineTo(19.69f)
                        quadToRelative(0.76f, 0f, 1.28f, 0.52f)
                        reflectiveQuadTo(21.5f, 6.31f)
                        verticalLineTo(17.69f)
                        quadToRelative(0f, 0.76f, -0.52f, 1.28f)
                        reflectiveQuadTo(19.69f, 19.5f)
                        horizontalLineTo(4.31f)
                        close()
                        moveToRelative(0f, -1.5f)
                        horizontalLineTo(19.69f)
                        quadToRelative(0.12f, 0f, 0.21f, -0.1f)
                        reflectiveQuadTo(20f, 17.69f)
                        verticalLineTo(8f)
                        horizontalLineTo(4f)
                        verticalLineToRelative(9.69f)
                        quadToRelative(0f, 0.12f, 0.1f, 0.21f)
                        reflectiveQuadTo(4.31f, 18f)
                        close()
                        moveTo(7.5f, 16.64f)
                        lineTo(6.46f, 15.6f)
                        lineTo(9.03f, 13f)
                        lineTo(6.43f, 10.4f)
                        lineTo(7.5f, 9.36f)
                        lineTo(11.14f, 13f)
                        lineTo(7.5f, 16.64f)
                        close()
                        moveToRelative(4.75f, 0.11f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineToRelative(5.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-5.5f)
                        close()
                    }
                }
                .build()
        return _terminal!!
    }

@Suppress("BackingPropertyNaming")
private var _terminal: ImageVector? = null
