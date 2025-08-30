package ru.vladislavsumin.qa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalQaColorScheme = staticCompositionLocalOf { QaColorScheme() }

@Immutable
data class QaColorScheme(
    val surface: Color = Color(0xFF1E1F22),
    val surfaceVariant: Color = Color(0xFF2B2D30),
    val onSurface: Color = Color(0xFFDFE1E5),
    val onSurfaceVariant: Color = Color(0xFFA1A2AA),
)

@Composable
fun QaTheme(content: @Composable () -> Unit) {
    val colorScheme = QaTheme.colorScheme
    MaterialTheme(
        colorScheme = darkColorScheme(
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            onSurfaceVariant = colorScheme.onSurfaceVariant,
        ),
        content = content,
    )
}

object QaTheme {
    val colorScheme: QaColorScheme
        @Composable @ReadOnlyComposable get() = LocalQaColorScheme.current
}
