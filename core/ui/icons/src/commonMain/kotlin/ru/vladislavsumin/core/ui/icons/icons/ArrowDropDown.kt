package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val arrow_drop_down: ImageVector
    get() {
        if (_arrow_drop_down != null) {
            return _arrow_drop_down!!
        }
        _arrow_drop_down =
            ImageVector.Builder(
                name = "arrow_drop_down",
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
                        moveTo(12f, 14.65f)
                        lineTo(7.6f, 10.25f)
                        horizontalLineTo(16.4f)
                        lineTo(12f, 14.65f)
                        close()
                    }
                }
                .build()
        return _arrow_drop_down!!
    }

@Suppress("BackingPropertyNaming")
private var _arrow_drop_down: ImageVector? = null
