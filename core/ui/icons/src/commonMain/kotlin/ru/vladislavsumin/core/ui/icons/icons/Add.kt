package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val add: ImageVector
    get() {
        if (_add != null) {
            return _add!!
        }
        _add =
            ImageVector.Builder(
                name = "add",
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
                        moveTo(11.25f, 12.75f)
                        horizontalLineTo(5.5f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineToRelative(5.75f)
                        verticalLineTo(5.5f)
                        horizontalLineToRelative(1.5f)
                        verticalLineToRelative(5.75f)
                        horizontalLineTo(18.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(12.75f)
                        verticalLineTo(18.5f)
                        horizontalLineToRelative(-1.5f)
                        verticalLineTo(12.75f)
                        close()
                    }
                }
                .build()
        return _add!!
    }

@Suppress("BackingPropertyNaming")
private var _add: ImageVector? = null
