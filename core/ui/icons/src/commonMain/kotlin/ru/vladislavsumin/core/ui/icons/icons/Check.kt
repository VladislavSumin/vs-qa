package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val check: ImageVector
    get() {
        if (_check != null) {
            return _check!!
        }
        _check =
            ImageVector.Builder(
                name = "check",
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
                        moveTo(9.55f, 17.65f)
                        lineTo(4.22f, 12.32f)
                        lineTo(5.28f, 11.25f)
                        lineToRelative(4.27f, 4.27f)
                        lineTo(18.72f, 6.35f)
                        lineToRelative(1.07f, 1.07f)
                        lineTo(9.55f, 17.65f)
                        close()
                    }
                }
                .build()
        return _check!!
    }

@Suppress("BackingPropertyNaming")
private var _check: ImageVector? = null
