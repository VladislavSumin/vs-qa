package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.ScreenParams
import java.nio.file.Path

@Serializable
data class LogViewerScreenParams(
    val logPath: Path,
    val mappingPath: Path?,
) : ScreenParams
