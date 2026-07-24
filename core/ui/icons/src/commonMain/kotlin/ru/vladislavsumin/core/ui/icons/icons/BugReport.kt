package ru.vladislavsumin.core.ui.icons.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val bug_report: ImageVector
    get() {
        if (_bug_report != null) {
            return _bug_report!!
        }
        _bug_report =
            ImageVector.Builder(
                name = "bug_report",
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
                        moveTo(12f, 19f)
                        quadToRelative(1.65f, 0f, 2.83f, -1.18f)
                        reflectiveQuadTo(16f, 15f)
                        verticalLineTo(11f)
                        quadTo(16f, 9.35f, 14.83f, 8.17f)
                        reflectiveQuadTo(12f, 7f)
                        reflectiveQuadTo(9.18f, 8.17f)
                        reflectiveQuadTo(8f, 11f)
                        verticalLineToRelative(4f)
                        quadToRelative(0f, 1.65f, 1.18f, 2.82f)
                        reflectiveQuadTo(12f, 19f)
                        close()
                        moveTo(10.15f, 15.75f)
                        horizontalLineToRelative(3.69f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineTo(10.15f)
                        verticalLineToRelative(1.5f)
                        close()
                        moveToRelative(0f, -4f)
                        horizontalLineToRelative(3.69f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineTo(10.15f)
                        verticalLineToRelative(1.5f)
                        close()
                        moveTo(12f, 13f)
                        close()
                        moveToRelative(0f, 7.5f)
                        quadToRelative(-1.49f, 0f, -2.75f, -0.73f)
                        reflectiveQuadToRelative(-2f, -2.02f)
                        horizontalLineTo(4.5f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineTo(6.68f)
                        quadTo(6.53f, 15.63f, 6.51f, 15f)
                        reflectiveQuadTo(6.5f, 13.75f)
                        horizontalLineToRelative(-2f)
                        verticalLineToRelative(-1.5f)
                        horizontalLineToRelative(2f)
                        quadToRelative(0f, -0.63f, 0f, -1.26f)
                        reflectiveQuadTo(6.68f, 9.75f)
                        horizontalLineTo(4.5f)
                        verticalLineTo(8.25f)
                        horizontalLineTo(7.24f)
                        quadTo(7.6f, 7.64f, 8.09f, 7.13f)
                        reflectiveQuadTo(9.22f, 6.28f)
                        lineTo(7.52f, 4.55f)
                        lineTo(8.55f, 3.52f)
                        lineToRelative(2.13f, 2.13f)
                        quadToRelative(0.64f, -0.2f, 1.3f, -0.2f)
                        reflectiveQuadToRelative(1.3f, 0.2f)
                        lineTo(15.47f, 3.52f)
                        lineTo(16.5f, 4.55f)
                        lineTo(14.77f, 6.28f)
                        quadToRelative(0.63f, 0.35f, 1.13f, 0.84f)
                        reflectiveQuadToRelative(0.86f, 1.13f)
                        horizontalLineTo(19.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(17.32f)
                        quadToRelative(0.17f, 0.61f, 0.17f, 1.24f)
                        reflectiveQuadToRelative(0f, 1.26f)
                        horizontalLineToRelative(2f)
                        verticalLineToRelative(1.5f)
                        horizontalLineToRelative(-2f)
                        quadToRelative(0f, 0.63f, -0.01f, 1.25f)
                        reflectiveQuadToRelative(-0.16f, 1.25f)
                        horizontalLineTo(19.5f)
                        verticalLineToRelative(1.5f)
                        horizontalLineTo(16.76f)
                        quadToRelative(-0.74f, 1.28f, -2f, 2.02f)
                        reflectiveQuadTo(12f, 20.5f)
                        close()
                    }
                }
                .build()
        return _bug_report!!
    }

@Suppress("BackingPropertyNaming")
private var _bug_report: ImageVector? = null
