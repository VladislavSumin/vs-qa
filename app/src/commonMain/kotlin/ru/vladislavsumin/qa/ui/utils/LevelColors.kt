package ru.vladislavsumin.qa.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.vladislavsumin.core.logger.api.logger

object LevelColors {
    @Composable
    fun getLevelColor(level: String): Color {
        return when (level) {
            "E" -> MaterialTheme.colorScheme.error
            "W" -> Color.Yellow // TODO сделать тему с цветами
            "I" -> MaterialTheme.colorScheme.onSurface
            "D" -> MaterialTheme.colorScheme.onSurfaceVariant
            "V" -> MaterialTheme.colorScheme.outline
            else -> {
                LevelColorLogger.w { "Unknown level $level" }
                MaterialTheme.colorScheme.onSurface
            }
        }
    }

    private val LevelColorLogger = logger("level-color")
}
