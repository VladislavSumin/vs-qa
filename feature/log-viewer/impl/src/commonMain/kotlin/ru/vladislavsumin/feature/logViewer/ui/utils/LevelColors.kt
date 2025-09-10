package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.vladislavsumin.core.logger.api.logger
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

object LevelColors {
    @Composable
    fun getLevelColor(level: String): Pair<Color, Color> {
        return when (level) {
            "E" -> QaTheme.colorScheme.logError to QaTheme.colorScheme.logOnError
            "W" -> QaTheme.colorScheme.logWarn to QaTheme.colorScheme.logOnWarn
            "I" -> QaTheme.colorScheme.logInfo to QaTheme.colorScheme.logOnInfo
            "D" -> QaTheme.colorScheme.logDebug to QaTheme.colorScheme.logOnDebug
            "V" -> QaTheme.colorScheme.logTrace to QaTheme.colorScheme.logOnTrace
            else -> {
                LevelColorLogger.w { "Unknown level $level" }
                QaTheme.colorScheme.logError to QaTheme.colorScheme.logOnError
            }
        }
    }

    private val LevelColorLogger = logger("level-color")
}
