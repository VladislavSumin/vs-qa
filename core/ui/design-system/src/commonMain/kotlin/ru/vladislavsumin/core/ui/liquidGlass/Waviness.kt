package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported
import org.intellij.lang.annotations.Language

/**
 * AGSL-шейдер «кривого стекла» (некачественное бутылочное): лёгкие искажения по всей
 * поверхности. Процедурный value-noise с тремя октавами FBM — без внешних текстур,
 * работает и на десктопе, и на Android. Фаза [phase] медленно сдвигает поле шума — стекло «дышит».
 */
@Language("AGSL")
private const val WavinessShaderString = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float  amount;
uniform float  frequency;
uniform float  phase;

float hash(float2 p) {
    return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
}

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),
               mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 3; i++) {
        v += amp * noise(p);
        p *= 2.0;
        amp *= 0.5;
    }
    return v / 0.875;
}

half4 main(float2 coord) {
    float2 p = coord + offset;
    // Круговой дрейф: при phase = 1 сдвиг совпадает с phase = 0, поэтому цикл бесшовный.
    float2 np = p * (frequency / min(size.x, size.y)) +
            float2(cos(phase * 6.2831853), sin(phase * 6.2831853)) * 1.5;
    float2 n = float2(fbm(np), fbm(np + float2(7.3, 3.1)));
    float2 disp = (n - 0.5) * 2.0 * amount;
    float2 sampled = clamp(coord + disp, float2(1.0), size - float2(1.0));
    return content.eval(sampled);
}"""

/**
 * Фаза дрейфа «кривого стекла»: медленный бесконечный цикл (~12 секунд),
 * сдвигающий поле шума. Чтение фазы в эффектах наблюдается библиотекой,
 * поэтому стекло перерисовывается каждый кадр.
 */
@Composable
fun rememberWavinessPhase(): State<Float> {
    val transition = rememberInfiniteTransition(label = "vs-waviness")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
        ),
        label = "vs-waviness-phase",
    )
}

/**
 * Добавляет в стекло эффект «кривого стекла»: лёгкие искажения выборки по всей поверхности.
 *
 * Рекомендуется вызывать последним в цепочке эффектов — тогда искажается всё поле,
 * включая краевую рефракцию и лунку нажатия (как у толстого некачественного стекла).
 * Выборка клампится в границы слоя, поэтому у краёв и скруглений артефактов нет.
 *
 * @param amount максимальное смещение выборки (в пикселях), для лёгкого эффекта ~0.05 от
 *   меньшей стороны поверхности.
 * @param frequency «волн» шума на меньшую сторону поверхности.
 * @param phase фаза дрейфа 0..1 (см. [rememberWavinessPhase]).
 */
fun BackdropEffectScope.waviness(amount: Float, frequency: Float, phase: Float,) {
    if (!isRuntimeShaderSupported()) return
    if (amount <= 0f) return
    runtimeShaderEffect(
        key = "GlassWaviness",
        shaderString = WavinessShaderString,
        uniformShaderName = "content",
    ) {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("offset", -padding, -padding)
        setFloatUniform("amount", amount)
        setFloatUniform("frequency", frequency)
        setFloatUniform("phase", phase)
    }
}
