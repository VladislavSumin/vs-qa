package ru.vladislavsumin.qa.ui.utils

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.vladislavsumin.core.logger.api.logger

object TagColors {
    @Composable
    fun getTagColor(tag: String): Color {
        return when (tag) {
            "E" -> MaterialTheme.colors.error
            "W" -> Color.Yellow // TODO сделать тему с цветами
            "I" -> MaterialTheme.colors.primary
            "D" -> MaterialTheme.colors.primary
            "T" -> MaterialTheme.colors.secondary
            else -> {
                TagColorLogger.w { "Unknown tag $tag" }
                MaterialTheme.colors.primary
            }
        }
    }

    private val TagColorLogger = logger("tag-color")
}
