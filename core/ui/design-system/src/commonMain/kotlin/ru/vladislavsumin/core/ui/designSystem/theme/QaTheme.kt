package ru.vladislavsumin.core.ui.designSystem.theme

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlin.io.path.Path

internal val LocalQaColorScheme = staticCompositionLocalOf { QaColorScheme() }

@Composable
fun QaTheme(content: @Composable () -> Unit) {
    val colorScheme = remember {
        // TODO вынести в общий код.
        val home = System.getProperty("user.home")
        val scheme = Path(home).resolve(".vs-qa/theme.yaml").toFile()
        if (scheme.exists()) {
            Yaml.default.decodeFromString<QaColorScheme>(scheme.readText())
        } else {
            QaColorScheme()
        }
    }
    CompositionLocalProvider(
        LocalQaColorScheme provides colorScheme,
        LocalMinimumInteractiveComponentSize provides 24.dp,
    ) {
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
