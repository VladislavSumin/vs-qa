package ru.vladislavsumin.core.ui.designSystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Suppress("MagicNumber")
@Serializable
data class QaColorScheme(
    @Serializable(with = ColorAsStringSerializer::class)
    val surface: Color = Color(0xFF1E1F22),
    @Serializable(with = ColorAsStringSerializer::class)
    val surfaceVariant: Color = Color(0xFF2B2D30),
    @Serializable(with = ColorAsStringSerializer::class)
    val onSurface: Color = Color(0xFFDFE1E5),
    @Serializable(with = ColorAsStringSerializer::class)
    val onSurfaceVariant: Color = Color(0xFFA1A2AA),

    @Serializable(with = ColorAsStringSerializer::class)
    val logHighlight: Color = Color(0x80A0A0A0),
    @Serializable(with = ColorAsStringSerializer::class)
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
@Serializable
data class LogColor(
    @Serializable(with = ColorAsStringSerializer::class)
    val primary: Color,
    @Serializable(with = ColorAsStringSerializer::class)
    val background: Color = primary,
    @Serializable(with = ColorAsStringSerializer::class)
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

private object ColorAsStringSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        error("Serialization non implemented")
    }

    override fun deserialize(decoder: Decoder): Color {
        val string = decoder.decodeString()
        @Suppress("MagicNumber")
        return Color(string.toLong(16))
    }
}
