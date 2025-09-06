package ru.vladislavsumin.core.ui.designSystem.theme

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

internal val LocalQaColorScheme = staticCompositionLocalOf { QaColorScheme() }

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
