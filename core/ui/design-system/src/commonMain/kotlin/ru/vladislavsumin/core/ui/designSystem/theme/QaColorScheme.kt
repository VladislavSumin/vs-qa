package ru.vladislavsumin.core.ui.designSystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
@Suppress("MagicNumber")
data class QaColorScheme(
    val surface: Color = Color(0xFF1E1F22),
    val surfaceVariant: Color = Color(0xFF2B2D30),
    val onSurface: Color = Color(0xFFDFE1E5),
    val onSurfaceVariant: Color = Color(0xFFA1A2AA),

    // Logs
    val logError: Color = Color(0xFFCF5B56),
    val logOnError: Color = Color(0xFF000000),
    val logWarn: Color = Color(0xFFBBB528),
    val logOnWarn: Color = Color(0xFF000000),
    val logInfo: Color = Color(0xFF6A8759),
    val logOnInfo: Color = Color(0xFFFFFFFF),
    val logDebug: Color = Color(0xFF305D78),
    val logOnDebug: Color = Color(0xFFFFFFFF),
    val logTrace: Color = Color(0xFFA1A2AA),
    val logOnTrace: Color = Color(0xFF000000),
)
