package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val settings: ImageVector
    get() {
        if (_settings != null) {
            return _settings!!
        }
        _settings =
            ImageVector.Builder(
                name = "settings",
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
                        moveTo(9.69f, 21.5f)
                        lineTo(9.31f, 18.45f)
                        quadTo(8.91f, 18.32f, 8.49f, 18.08f)
                        reflectiveQuadTo(7.73f, 17.56f)
                        lineTo(4.91f, 18.75f)
                        lineToRelative(-2.31f, -4f)
                        lineTo(5.04f, 12.91f)
                        quadTo(5.01f, 12.68f, 5f, 12.46f)
                        reflectiveQuadTo(4.98f, 12.01f)
                        quadTo(4.98f, 11.8f, 5f, 11.58f)
                        reflectiveQuadTo(5.04f, 11.09f)
                        lineTo(2.6f, 9.25f)
                        lineTo(4.91f, 5.27f)
                        lineTo(7.72f, 6.45f)
                        quadTo(8.08f, 6.17f, 8.5f, 5.93f)
                        reflectiveQuadTo(9.3f, 5.55f)
                        lineTo(9.69f, 2.5f)
                        horizontalLineToRelative(4.62f)
                        lineToRelative(0.38f, 3.06f)
                        quadToRelative(0.45f, 0.16f, 0.81f, 0.38f)
                        reflectiveQuadToRelative(0.74f, 0.51f)
                        lineTo(19.09f, 5.27f)
                        lineTo(21.4f, 9.25f)
                        lineToRelative(-2.48f, 1.87f)
                        quadToRelative(0.05f, 0.24f, 0.06f, 0.45f)
                        reflectiveQuadToRelative(0f, 0.42f)
                        quadToRelative(0f, 0.2f, -0.01f, 0.41f)
                        reflectiveQuadTo(18.9f, 12.9f)
                        lineToRelative(2.46f, 1.85f)
                        lineToRelative(-2.31f, 4f)
                        lineToRelative(-2.82f, -1.2f)
                        quadToRelative(-0.37f, 0.3f, -0.76f, 0.52f)
                        reflectiveQuadToRelative(-0.79f, 0.37f)
                        lineTo(14.31f, 21.5f)
                        horizontalLineTo(9.69f)
                        close()
                        moveTo(11f, 20f)
                        horizontalLineToRelative(1.97f)
                        lineToRelative(0.36f, -2.68f)
                        quadToRelative(0.77f, -0.2f, 1.4f, -0.57f)
                        reflectiveQuadToRelative(1.22f, -0.95f)
                        lineToRelative(2.48f, 1.04f)
                        lineToRelative(0.98f, -1.7f)
                        lineTo(17.25f, 13.52f)
                        quadToRelative(0.13f, -0.39f, 0.17f, -0.76f)
                        reflectiveQuadTo(17.46f, 12f)
                        quadToRelative(0f, -0.39f, -0.05f, -0.75f)
                        reflectiveQuadTo(17.25f, 10.5f)
                        lineTo(19.43f, 8.85f)
                        lineTo(18.45f, 7.15f)
                        lineTo(15.94f, 8.21f)
                        quadTo(15.43f, 7.67f, 14.73f, 7.26f)
                        reflectiveQuadTo(13.32f, 6.68f)
                        lineTo(13f, 4f)
                        horizontalLineTo(11.02f)
                        lineTo(10.68f, 6.67f)
                        quadTo(9.92f, 6.85f, 9.27f, 7.22f)
                        reflectiveQuadTo(8.03f, 8.18f)
                        lineTo(5.55f, 7.15f)
                        lineTo(4.57f, 8.85f)
                        lineToRelative(2.16f, 1.61f)
                        quadTo(6.6f, 10.82f, 6.55f, 11.2f)
                        reflectiveQuadTo(6.5f, 12.01f)
                        quadToRelative(0f, 0.39f, 0.05f, 0.77f)
                        reflectiveQuadToRelative(0.17f, 0.74f)
                        lineTo(4.57f, 15.15f)
                        lineToRelative(0.98f, 1.7f)
                        lineTo(8.03f, 15.8f)
                        quadToRelative(0.57f, 0.58f, 1.22f, 0.96f)
                        reflectiveQuadToRelative(1.43f, 0.57f)
                        lineTo(11f, 20f)
                        close()
                        moveToRelative(1.01f, -5f)
                        quadToRelative(1.25f, 0f, 2.12f, -0.88f)
                        reflectiveQuadTo(15.01f, 12f)
                        reflectiveQuadTo(14.14f, 9.88f)
                        reflectiveQuadTo(12.01f, 9f)
                        quadTo(10.75f, 9f, 9.88f, 9.88f)
                        reflectiveQuadTo(9.01f, 12f)
                        reflectiveQuadToRelative(0.87f, 2.12f)
                        reflectiveQuadTo(12.01f, 15f)
                        close()
                        moveTo(12f, 12f)
                        close()
                    }
                }
                .build()
        return _settings!!
    }

@Suppress("BackingPropertyNaming")
private var _settings: ImageVector? = null
