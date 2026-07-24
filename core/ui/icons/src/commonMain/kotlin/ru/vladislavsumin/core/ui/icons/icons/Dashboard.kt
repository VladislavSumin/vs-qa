package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val dashboard: ImageVector
    get() {
        if (_dashboard != null) {
            return _dashboard!!
        }
        _dashboard =
            ImageVector.Builder(
                name = "dashboard",
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
                        moveTo(13.25f, 9f)
                        verticalLineTo(3.5f)
                        horizontalLineTo(20.5f)
                        verticalLineTo(9f)
                        horizontalLineTo(13.25f)
                        close()
                        moveTo(3.5f, 12.5f)
                        verticalLineToRelative(-9f)
                        horizontalLineToRelative(7.25f)
                        verticalLineToRelative(9f)
                        horizontalLineTo(3.5f)
                        close()
                        moveToRelative(9.75f, 8f)
                        verticalLineToRelative(-9f)
                        horizontalLineTo(20.5f)
                        verticalLineToRelative(9f)
                        horizontalLineTo(13.25f)
                        close()
                        moveToRelative(-9.75f, 0f)
                        verticalLineTo(15f)
                        horizontalLineToRelative(7.25f)
                        verticalLineToRelative(5.5f)
                        horizontalLineTo(3.5f)
                        close()
                        moveTo(5f, 11f)
                        horizontalLineTo(9.25f)
                        verticalLineTo(5f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(6f)
                        close()
                        moveToRelative(9.75f, 8f)
                        horizontalLineTo(19f)
                        verticalLineTo(13f)
                        horizontalLineTo(14.75f)
                        verticalLineToRelative(6f)
                        close()
                        moveToRelative(0f, -11.5f)
                        horizontalLineTo(19f)
                        verticalLineTo(5f)
                        horizontalLineTo(14.75f)
                        verticalLineTo(7.5f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(9.25f)
                        verticalLineTo(16.5f)
                        horizontalLineTo(5f)
                        verticalLineTo(19f)
                        close()
                        moveTo(9.25f, 11f)
                        close()
                        moveToRelative(5.5f, -3.5f)
                        close()
                        moveToRelative(0f, 5.5f)
                        close()
                        moveToRelative(-5.5f, 3.5f)
                        close()
                    }
                }
                .build()
        return _dashboard!!
    }

@Suppress("BackingPropertyNaming")
private var _dashboard: ImageVector? = null
