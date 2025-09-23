package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import java.nio.file.Path

@Serializable
data class LogViewerScreenParams(
    val logPath: Path,
) : IntentScreenParams<LogViewerScreenIntent>

sealed interface LogViewerScreenIntent : ScreenIntent {
    /**
     * Применяет или заменяет текущий mapping.
     */
    data class OpenMapping(val mappingPath: Path) : LogViewerScreenIntent
}
