package ru.vladislavsumin.qa.ui.theme

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val LocalQaColorScheme = staticCompositionLocalOf { QaColorScheme() }

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

@Composable
fun QaTheme(content: @Composable () -> Unit) {
    val colorScheme = QaTheme.colorScheme
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 24.dp) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                surface = colorScheme.surface,
                onSurface = colorScheme.onSurface,
                onSurfaceVariant = colorScheme.onSurfaceVariant,
            ),
            content = content,
        )
    }
}

object QaTheme {
    val colorScheme: QaColorScheme
        @Composable @ReadOnlyComposable
        get() = LocalQaColorScheme.current
}
