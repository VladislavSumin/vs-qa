package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val text_increase: ImageVector
    get() {
        if (_text_increase != null) {
            return _text_increase!!
        }
        _text_increase =
            ImageVector.Builder(
                name = "text_increase",
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
                        moveTo(1.31f, 18.75f)
                        lineTo(6.58f, 5.25f)
                        horizontalLineTo(8.31f)
                        lineToRelative(5.27f, 13.5f)
                        horizontalLineToRelative(-1.8f)
                        lineToRelative(-1.36f, -3.6f)
                        horizontalLineTo(4.43f)
                        lineToRelative(-1.36f, 3.6f)
                        horizontalLineTo(1.31f)
                        close()
                        moveToRelative(3.67f, -5.1f)
                        horizontalLineTo(9.87f)
                        lineTo(7.49f, 7.35f)
                        horizontalLineTo(7.37f)
                        lineToRelative(-2.4f, 6.3f)
                        close()
                        moveToRelative(13.22f, 2.1f)
                        verticalLineToRelative(-3f)
                        horizontalLineToRelative(-3f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(-3f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(3f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-3f)
                        verticalLineToRelative(3f)
                        horizontalLineToRelative(-1.5f)
                        close()
                    }
                }
                .build()
        return _text_increase!!
    }

@Suppress("BackingPropertyNaming")
private var _text_increase: ImageVector? = null
