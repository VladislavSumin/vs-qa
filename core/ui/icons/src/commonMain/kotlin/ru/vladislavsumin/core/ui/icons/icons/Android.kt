package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val android: ImageVector
    get() {
        if (_android != null) {
            return _android!!
        }
        _android =
            ImageVector.Builder(
                name = "android",
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
                        moveTo(1.85f, 17.49f)
                        quadTo(2.08f, 15.07f, 3.32f, 13.08f)
                        reflectiveQuadTo(6.53f, 9.85f)
                        lineTo(4.76f, 6.88f)
                        quadTo(4.55f, 6.56f, 4.65f, 6.19f)
                        reflectiveQuadTo(5.08f, 5.63f)
                        quadTo(5.4f, 5.43f, 5.77f, 5.52f)
                        reflectiveQuadTo(6.33f, 5.94f)
                        lineTo(8.17f, 9f)
                        quadTo(9.05f, 8.63f, 10.01f, 8.45f)
                        reflectiveQuadTo(12f, 8.26f)
                        reflectiveQuadToRelative(1.99f, 0.19f)
                        reflectiveQuadTo(15.83f, 9f)
                        lineTo(17.67f, 5.94f)
                        quadTo(17.86f, 5.62f, 18.23f, 5.52f)
                        reflectiveQuadToRelative(0.69f, 0.11f)
                        quadToRelative(0.32f, 0.19f, 0.42f, 0.56f)
                        reflectiveQuadToRelative(-0.1f, 0.69f)
                        lineTo(17.47f, 9.85f)
                        quadToRelative(1.96f, 1.24f, 3.21f, 3.23f)
                        reflectiveQuadToRelative(1.47f, 4.41f)
                        horizontalLineTo(1.85f)
                        close()
                        moveTo(17.55f, 14f)
                        quadToRelative(0.03f, -0.4f, -0.21f, -0.8f)
                        reflectiveQuadTo(16.72f, 12.69f)
                        reflectiveQuadToRelative(-0.68f, 0.09f)
                        reflectiveQuadToRelative(-0.35f, 0.6f)
                        reflectiveQuadToRelative(0.21f, 0.79f)
                        reflectiveQuadToRelative(0.62f, 0.51f)
                        reflectiveQuadToRelative(0.68f, -0.09f)
                        reflectiveQuadTo(17.55f, 14f)
                        close()
                        moveTo(7.48f, 14.68f)
                        quadTo(7.86f, 14.57f, 8.1f, 14.17f)
                        quadTo(8.35f, 13.78f, 8.32f, 13.38f)
                        reflectiveQuadTo(7.98f, 12.77f)
                        reflectiveQuadTo(7.29f, 12.68f)
                        reflectiveQuadTo(6.67f, 13.2f)
                        quadTo(6.42f, 13.58f, 6.46f, 13.98f)
                        reflectiveQuadToRelative(0.35f, 0.61f)
                        reflectiveQuadToRelative(0.68f, 0.09f)
                        close()
                    }
                }
                .build()
        return _android!!
    }

@Suppress("BackingPropertyNaming")
private var _android: ImageVector? = null
