package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val help: ImageVector
    get() {
        if (_help != null) {
            return _help!!
        }
        _help =
            ImageVector.Builder(
                name = "help",
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
                        moveTo(12.71f, 17.32f)
                        quadToRelative(0.3f, -0.3f, 0.3f, -0.72f)
                        reflectiveQuadToRelative(-0.3f, -0.72f)
                        reflectiveQuadToRelative(-0.72f, -0.3f)
                        reflectiveQuadToRelative(-0.72f, 0.3f)
                        reflectiveQuadToRelative(-0.3f, 0.72f)
                        reflectiveQuadToRelative(0.3f, 0.72f)
                        reflectiveQuadToRelative(0.72f, 0.3f)
                        reflectiveQuadToRelative(0.72f, -0.3f)
                        close()
                        moveTo(11.28f, 14.03f)
                        horizontalLineToRelative(1.41f)
                        quadTo(12.71f, 13.3f, 12.9f, 12.85f)
                        reflectiveQuadToRelative(0.96f, -1.17f)
                        quadToRelative(0.66f, -0.66f, 1.01f, -1.22f)
                        reflectiveQuadTo(15.22f, 9.15f)
                        quadToRelative(0f, -1.29f, -0.93f, -2.02f)
                        reflectiveQuadTo(12.1f, 6.4f)
                        quadToRelative(-1.25f, 0f, -2.07f, 0.67f)
                        reflectiveQuadTo(8.86f, 8.65f)
                        lineToRelative(1.28f, 0.52f)
                        quadToRelative(0.18f, -0.5f, 0.63f, -0.97f)
                        reflectiveQuadTo(12.08f, 7.72f)
                        quadToRelative(0.89f, 0f, 1.31f, 0.49f)
                        reflectiveQuadToRelative(0.42f, 1.07f)
                        quadToRelative(0f, 0.51f, -0.29f, 0.93f)
                        reflectiveQuadToRelative(-0.74f, 0.82f)
                        quadToRelative(-0.98f, 0.89f, -1.24f, 1.42f)
                        reflectiveQuadToRelative(-0.26f, 1.59f)
                        close()
                        moveTo(12f, 21.5f)
                        quadToRelative(-1.97f, 0f, -3.71f, -0.75f)
                        reflectiveQuadTo(5.28f, 18.72f)
                        reflectiveQuadTo(3.25f, 15.71f)
                        reflectiveQuadTo(2.5f, 12f)
                        reflectiveQuadTo(3.25f, 8.3f)
                        reflectiveQuadTo(5.28f, 5.28f)
                        reflectiveQuadTo(8.29f, 3.25f)
                        reflectiveQuadTo(12f, 2.5f)
                        reflectiveQuadTo(15.7f, 3.25f)
                        reflectiveQuadToRelative(3.02f, 2.03f)
                        reflectiveQuadToRelative(2.03f, 3.02f)
                        reflectiveQuadTo(21.5f, 12f)
                        reflectiveQuadTo(20.75f, 15.7f)
                        reflectiveQuadToRelative(-2.03f, 3.02f)
                        reflectiveQuadToRelative(-3.02f, 2.03f)
                        reflectiveQuadTo(12f, 21.5f)
                        close()
                        moveTo(12f, 20f)
                        quadToRelative(3.35f, 0f, 5.68f, -2.32f)
                        reflectiveQuadTo(20f, 12f)
                        reflectiveQuadTo(17.68f, 6.32f)
                        reflectiveQuadTo(12f, 4f)
                        reflectiveQuadTo(6.33f, 6.32f)
                        reflectiveQuadTo(4f, 12f)
                        reflectiveQuadToRelative(2.33f, 5.68f)
                        reflectiveQuadTo(12f, 20f)
                        close()
                        moveToRelative(0f, -8f)
                        close()
                    }
                }
                .build()
        return _help!!
    }

@Suppress("BackingPropertyNaming")
private var _help: ImageVector? = null
