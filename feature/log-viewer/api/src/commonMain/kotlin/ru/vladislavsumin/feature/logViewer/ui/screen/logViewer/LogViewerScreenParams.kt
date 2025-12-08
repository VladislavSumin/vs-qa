package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import java.nio.file.Path
import kotlin.io.path.Path

@Serializable
data class LogViewerScreenParams(
    @Serializable(PathSerializer::class)
    val logPath: Path,
) : IntentScreenParams<LogViewerScreenIntent>

sealed interface LogViewerScreenIntent : ScreenIntent {
    /**
     * Применяет или заменяет текущий mapping.
     */
    data class OpenMapping(val mappingPath: Path) : LogViewerScreenIntent
}

// TODO временный фикс для андроид что бы не падать при сериализации, полноценный фикс нужен в навигации.
private object PathSerializer : KSerializer<Path> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Path) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Path {
        return Path(decoder.decodeString())
    }
}
