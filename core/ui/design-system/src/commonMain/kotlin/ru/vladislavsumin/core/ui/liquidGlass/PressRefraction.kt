package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.ui.geometry.Offset
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported
import org.intellij.lang.annotations.Language

/**
 * Добавляет в стекло эффект «вдавленного стекла» в точке касания (нажатие или наведение).
 *
 * Должен вызываться последним в цепочке эффектов, чтобы сэмплировать уже
 * размытое и преломлённое содержимое. Выборка клампится в границы слоя, поэтому
 * эффект безопасен у краёв и на скруглениях (не читает прозрачность за пределами).
 *
 * @param amount суммарная интенсивность эффекта 0..1 (например, max из глубины нажатия
 *   и усиленной интенсивности наведения, анимируемой отдельно).
 * @param touchPos точка касания/наведения в локальных координатах поверхности.
 * @param radius радиус лунки.
 * @param strength максимальное смещение выборки (в пикселях).
 */
fun BackdropEffectScope.pressRefraction(amount: Float, touchPos: Offset, radius: Float, strength: Float) {
    if (!isRuntimeShaderSupported()) return
    if (amount <= 0f) return
    runtimeShaderEffect(
        key = "PressRefraction",
        shaderString = SHADER,
        uniformShaderName = "content",
    ) {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("offset", -padding, -padding)
        setFloatUniform("touchPos", touchPos.x, touchPos.y)
        setFloatUniform("amount", amount)
        setFloatUniform("radius", radius)
        setFloatUniform("strength", strength)
    }
}

/**
 * AGSL-шейдер «продавливания» стекла: гауссова лунка вокруг точки касания,
 * сэмплирование тянется к точке нажатия (локальное увеличение — как будто
 * стекло вдавили пальцем).
 *
 * Приём из Liquid-Glass-Android (QWEA0), файл GlassLensRenderer.kt:
 * https://github.com/QWEA0/Liquid-Glass-Android (лицензия Apache-2.0)
 */
@Language("AGSL")
private const val SHADER = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float2 touchPos;
uniform float  amount;
uniform float  radius;
uniform float  strength;

half4 main(float2 coord) {
    float2 p = coord + offset;
    float2 tp = p - touchPos;
    float tr = length(tp);
    float bump = amount * exp(-(tr * tr) / (radius * radius * 0.30));
    float2 disp = -(tp / max(tr, 1.0)) * (bump * strength);
    float2 sampled = clamp(coord + disp, float2(1.0), size - float2(1.0));
    return content.eval(sampled);
}"""
