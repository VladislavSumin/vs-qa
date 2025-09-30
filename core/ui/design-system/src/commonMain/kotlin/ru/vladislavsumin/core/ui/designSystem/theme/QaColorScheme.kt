package ru.vladislavsumin.core.ui.designSystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
@Suppress("MagicNumber")
data class QaColorScheme(
    val surface: Color = Color(0xFF1E1F22),
    val surfaceVariant: Color = Color(0xFF2B2D30),
    val onSurface: Color = Color(0xFFDFE1E5),
    val onSurfaceVariant: Color = Color(0xFFA1A2AA),

    val logHighlight: Color = Color(0x80A0A0A0),
    val logHighlightSelected: Color = Color(0xCC373AB1),

    // Logs
    val logFatal: LogColor = LogColor(
        primary = Color(0xFFFF6B68),
        background = Color(0xFF8B3C3C),
    ),
    val logError: LogColor = LogColor(
        primary = Color(0xFFFF6B68),
        background = Color(0xFFCF5B56),
    ),
    val logWarn: LogColor = LogColor(
        primary = Color(0xFFBBB529),
        background = Color(0xFFBBB529),
    ),
    val logInfo: LogColor = LogColor(
        primary = Color(0xFFABC023),
        background = Color(0xFF6A8759),
    ),
    val logDebug: LogColor = LogColor(
        primary = Color(0xFF299999),
        background = Color(0xFF305D78),
    ),
    val logTrace: LogColor = LogColor(
        primary = Color(0xFFBBBBBB),
        background = Color(0xFFD6D6D6),
    ),
)

/**
 * Цвета для определенного уровня логов.
 *
 * @param primary основной цвет текста логов.
 * @param background фон для максимального выделения записи (например в тегах).
 * @param onBackground цвет текста на фоне [background].
 */
@Immutable
data class LogColor(
    val primary: Color,
    val background: Color = primary,
    val onBackground: Color = contrastColorFor(background),
)

/**
 * Выбирает между [Color.Black] и [Color.White] как наиболее контрастным для [color].
 */
@Suppress("MagicNumber")
private fun contrastColorFor(color: Color): Color {
    val avg = (color.red + color.green + color.blue) / 3
    return if (avg > 0.5) Color.Black else Color.White
}
