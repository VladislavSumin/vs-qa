package ru.vladislavsumin.core.ui.designSystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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

    @Transient
    val tagColors: List<Color> = defaultTagColors(),
)

@Suppress("MagicNumber")
private fun defaultTagColors(): List<Color> = listOf(
    Color(0xFF6EC1E8), // голубой
    Color(0xFF5DD9B5), // бирюзовый
    Color(0xFF7ED96B), // зелёный
    Color(0xFFB8E86A), // лайм
    Color(0xFFE8D46A), // жёлтый
    Color(0xFFE8A85C), // оранжевый
    Color(0xFFE87A6A), // коралловый
    Color(0xFFE86A9E), // розовый
    Color(0xFFC97AE8), // сиреневый
    Color(0xFFA87AE8), // фиолетовый
    Color(0xFF7A8AE8), // синий
    Color(0xFF6AB8E8), // светло-синий
    Color(0xFF6AE8C9), // аква
    Color(0xFF8AE87A), // салатовый
    Color(0xFFE8B86A), // янтарный
    Color(0xFF5CB8E8), // тёмно-голубой
    Color(0xFF4AC9A8), // мятный
    Color(0xFF6BC95A), // изумрудный
    Color(0xFF9ED95A), // жёлто-зелёный
    Color(0xFFD9C05A), // золотистый
    Color(0xFFD98A4A), // тёмно-оранжевый
    Color(0xFFD96A5A), // терракотовый
    Color(0xFFD95A8E), // малиновый
    Color(0xFFB86AD9), // пурпурный
    Color(0xFF906AD9), // индиго
    Color(0xFF6A7AD9), // перванш
    Color(0xFF5A98D9), // стальной синий
    Color(0xFF5AD9B8), // циан
    Color(0xFF7AD96A), // зелёное яблоко
    Color(0xFFD9A85A), // медовый
    Color(0xFFC98AD9), // орхидея
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
