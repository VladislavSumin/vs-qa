package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.runtime.Composable
import ru.vladislavsumin.core.ui.designSystem.theme.LogColor
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logParser.domain.LogLevel

object LevelColors {
    @Composable
    fun getLevelColor(level: LogLevel): LogColor {
        return when (level) {
            LogLevel.FATAL -> QaTheme.colorScheme.logFatal
            LogLevel.ERROR -> QaTheme.colorScheme.logError
            LogLevel.WARN -> QaTheme.colorScheme.logWarn
            LogLevel.INFO -> QaTheme.colorScheme.logInfo
            LogLevel.DEBUG -> QaTheme.colorScheme.logDebug
            LogLevel.VERBOSE -> QaTheme.colorScheme.logTrace
        }
    }
}
