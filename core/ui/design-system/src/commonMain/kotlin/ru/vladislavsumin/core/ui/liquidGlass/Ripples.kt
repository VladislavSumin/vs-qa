package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported
import org.intellij.lang.annotations.Language

/**
 * Максимальное число одновременно учитываемых капель — размер массивов шейдера.
 * Пустые слоты помечаются отрицательным birth и пропускаются в цикле.
 */
private const val MAX_RIPPLES = 64

/**
 * AGSL-шейдер «волн на воде»: расходящиеся кольца от капель с затуханием.
 * Аналитические кольца без текстуры с памятью: состояние хранится только на стороне
 * Kotlin — список капель (позиция + время рождения), шейдер суммирует их каждый кадр.
 */
@Language("AGSL")
private val RipplesShaderString = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float  time;
uniform float  amount;
uniform float  wavelength;
uniform float  speed;
uniform float  decay;
uniform float  falloff;
uniform float  originX[$MAX_RIPPLES];
uniform float  originY[$MAX_RIPPLES];
uniform float  birth[$MAX_RIPPLES];
uniform float  strength[$MAX_RIPPLES];

half4 main(float2 coord) {
    float2 p = coord + offset;
    float k = 6.2831853 / wavelength;
    float2 disp = float2(0.0);
    // Граница цикла обязана быть константой (ограничение SkSL на десктопе),
    // поэтому пустые слоты помечаются отрицательным birth и пропускаются.
    for (int i = 0; i < $MAX_RIPPLES; i++) {
        if (birth[i] < 0.0) continue;
        float age = time - birth[i];
        float2 d = p - float2(originX[i], originY[i]);
        float r = length(d);
        float wave = sin(r * k - age * speed);
        // Зона удара: амплитуда плавно растёт от нуля в центре капли (иначе там
        // «дышащая» точка — огибающая на пике, а wave осциллирует по возрасту).
        float core = smoothstep(0.0, wavelength * 0.25, r);
        // Плавное появление капли (fade-in ~0.15с) и затухание по радиусу и времени.
        float fadeIn = smoothstep(0.0, 0.15, age);
        float envelope = fadeIn * core * exp(-(r * r) / (falloff * falloff)) * exp(-age * decay);
        float amp = wave * envelope * strength[i] * amount;
        disp += (d / max(r, 1.0)) * amp;
    }
    float2 sampled = clamp(coord + disp, float2(1.0), size - float2(1.0));
    return content.eval(sampled);
}"""

/**
 * Источник «капель» для эффекта волн: хранит до [maxRipples] последних капель
 * (позиция в локальных координатах поверхности + время рождения + сила)
 * и «часы» — секунды, обновляемые каждый кадр.
 */
class RippleSource(private val maxRipples: Int) {

    internal class Ripple(val x: Float, val y: Float, val birth: Float, val strength: Float)

    private val timeState = mutableFloatStateOf(0f)
    private val ripplesState = mutableStateOf(emptyList<Ripple>())

    val time: Float get() = timeState.floatValue
    internal val ripples: List<Ripple> get() = ripplesState.value

    /** Обновляет «часы» (вызывается каждый кадр). */
    fun tick(seconds: Float) {
        timeState.floatValue = seconds
    }

    /**
     * Спавнит каплю в [position] (локальные координаты поверхности).
     * Погасшие капли (старше [RIPPLE_LIFETIME]) вычищаются, чтобы не копить мёртвые слоты.
     *
     * @param position точка удара капли в локальных координатах поверхности (например,
     *   позиция курсора, можно с джиттером).
     * @param strength сила капли 0..1 — множитель амплитуды кольца (по умолчанию 1).
     */
    fun spawn(position: Offset, strength: Float = 1f) {
        val now = timeState.floatValue
        val updated = ripplesState.value
            .filter { now - it.birth <= RIPPLE_LIFETIME }
            .toMutableList()
        updated.add(Ripple(position.x, position.y, now, strength))
        while (updated.size > maxRipples) {
            updated.removeAt(0)
        }
        ripplesState.value = updated
    }

    private companion object {
        /** Жизнь капли: после этого срока она гаснет ниже ~1% и вычищается. */
        const val RIPPLE_LIFETIME = 4f
    }
}

/**
 * Создаёт [RippleSource] с «часами» на кадровом клоке (секунды обновляются каждый кадр).
 *
 * @param maxRipples максимальное число одновременно учитываемых капель. При превышении
 *   вытесняется самая старая капля. Должен быть не больше размера массивов шейдера
 *   (константа [MAX_RIPPLES], по умолчанию 64).
 */
@Composable
fun rememberRippleSource(maxRipples: Int = MAX_RIPPLES): RippleSource {
    val source = remember { RippleSource(maxRipples) }
    LaunchedEffect(source) {
        while (true) {
            withFrameNanos { nanos ->
                source.tick(nanos / 1_000_000_000f)
            }
        }
    }
    return source
}

/**
 * Добавляет в стекло эффект «волн на воде»: расходящиеся кольца от капель.
 *
 * Чтение состояния источника (время и список капель) внутри эффектов наблюдается
 * библиотекой, поэтому волны перерисовываются каждый кадр, пока есть капли.
 * Учитывается до [MAX_RIPPLES] капель, пустые слоты пропускаются; выборка клампится
 * в границы слоя — у краёв артефактов нет.
 *
 * Вклад каждой капли в смещение выборки (в пикселе [p]):
 * ```
 * age     = time − birth                        // возраст капли, с
 * r       = |p − origin|                        // расстояние от капли до пикселя
 * wave    = sin(r·2π/wavelength − age·speed)    // расходящееся кольцо, ±1
 * envelope = fadeIn · core · exp(−r²/falloff²) · exp(−age·decay)
 *                                                // fadeIn — плавное появление (~0.15с),
 *                                                // core — зона удара (0 в центре капли),
 *                                                // остальное — затухание по радиусу и времени
 * смещение += (p − origin)/max(r, 1) · wave · envelope · strength · amount
 * ```
 *
 * @param source источник капель: «часы» (секунды, обновляются каждый кадр) + список капель
 *   (позиция, время рождения, сила). Капли создаются через [RippleSource.spawn],
 *   см. [rememberRippleSource].
 * @param amount сила волны: максимальное смещение выборки в пикселях. Фактическое смещение
 *   равно amount, умноженному на envelope (0..1) и wave (±1), поэтому пиковая амплитуда
 *   достигается только у свежей капли близко к её кольцу. Больше → картинка сильнее
 *   изгибается. Хорошая отправная точка — ~0.1–0.15 от меньшей стороны поверхности.
 * @param wavelength длина волны кольца в пикселях — расстояние между соседними гребнями.
 *   Задаёт волновое число k = 2π/wavelength: меньше wavelength → кольца мельче и чаще,
 *   больше → шире и реже.
 * @param speed фазовая скорость кольца в радианах/секунду: как быстро кольцо разбегается
 *   от капли. Линейная скорость расширения ≈ speed·wavelength/(2π) px/с. Больше → волны
 *   бегут быстрее.
 * @param decay затухание амплитуды по времени, 1/секунду: множитель exp(−age·decay).
 *   При decay = 1.0 через 1с амплитуда ~37%, через 2.5с ~8% — капля «живёт» ~2.5с.
 *   Больше → капли гаснут быстрее, меньше → следы волн висят дольше.
 * @param falloff пространственная огибающая затухания в пикселях: множитель
 *   exp(−r²/falloff²) — насколько далеко от капли кольцо остаётся видимым.
 *   На расстоянии falloff амплитуда ~37%, на 2·falloff — практически ноль.
 *   Больше → волны достают дальше от точки удара.
 */
fun BackdropEffectScope.ripples(
    source: RippleSource,
    amount: Float,
    wavelength: Float,
    speed: Float,
    decay: Float,
    falloff: Float,
) {
    if (!isRuntimeShaderSupported()) return
    val ripples = source.ripples
    if (ripples.isEmpty() || amount <= 0f) return
    val time = source.time
    runtimeShaderEffect(
        key = "Ripples",
        shaderString = RipplesShaderString,
        uniformShaderName = "content",
    ) {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("offset", -padding, -padding)
        setFloatUniform("time", time)
        setFloatUniform("amount", amount)
        setFloatUniform("wavelength", wavelength)
        setFloatUniform("speed", speed)
        setFloatUniform("decay", decay)
        setFloatUniform("falloff", falloff)
        setFloatUniform("originX", FloatArray(MAX_RIPPLES) { i -> ripples.getOrNull(i)?.x ?: 0f })
        setFloatUniform("originY", FloatArray(MAX_RIPPLES) { i -> ripples.getOrNull(i)?.y ?: 0f })
        setFloatUniform("birth", FloatArray(MAX_RIPPLES) { i -> ripples.getOrNull(i)?.birth ?: -1f })
        setFloatUniform("strength", FloatArray(MAX_RIPPLES) { i -> ripples.getOrNull(i)?.strength ?: 0f })
    }
}
