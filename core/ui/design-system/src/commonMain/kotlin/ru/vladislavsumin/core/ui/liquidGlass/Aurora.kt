package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Создаёт анимированную «аврору» как содержимое стекла (CanvasBackdrop):
 * три световых пятна плавно дрейфуют; перерисовку каждого кадра ведёт
 * чтение фазы в draw-фазе.
 */
@Composable
fun rememberAuroraBackdrop(blobScale: Float = .6f): Backdrop {
    val auroraColors = AuroraColors.fromTheme()
    val transition = rememberInfiniteTransition(label = "vs-aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
        ),
        label = "vs-aurora-phase",
    )
    return rememberCanvasBackdrop { drawAurora(phase, auroraColors, blobScale = blobScale) }
}

/**
 * Рисует аврору из трёх мягких радиальных пятен, плавно дрейфующих вокруг [center].
 *
 * Позиции пятен заданы долями размера поверхности, а радиусы — от её длинной стороны,
 * поэтому аврора универсальна для любой формы: на вытянутой капсуле пятна растягиваются
 * вдоль длины до самых краёв, на квадратной — компактным треугольником, на высокой — по вертикали.
 * Радиусы достаточно велики, чтобы цвет доходил до краёв, а рефракция на кромке «цепляла» его.
 *
 * @param phase фаза анимации в диапазоне 0..1 (полный цикл — один оборот пятен).
 * @param colors цвета пятен.
 * @param center центр дрейфа пятен.
 * @param blobScale множитель интенсивности: радиусы пятен относительно длинной стороны поверхности.
 */
@Suppress("MagicNumber")
fun DrawScope.drawAurora(
    phase: Float,
    colors: AuroraColors,
    center: Offset = Offset(size.width / 2f, size.height / 2f),
    blobScale: Float = 1f,
) {
    val w = size.width
    val h = size.height
    val base = size.maxDimension
    val angle = phase * 2f * PI.toFloat()
    // Дрейф пропорционален осям: на вытянутой поверхности движение заметно по обеим осям.
    val driftX = w * 0.1f
    val driftY = h * 0.3f

    val mainCenter = center + Offset(-w * 0.22f, 0f) + Offset(driftX * cos(angle), driftY * sin(angle))
    val mainRadius = base * 0.6f * blobScale
    drawAuroraBlob(center = mainCenter, radius = mainRadius, color = colors.main, alpha = 0.8f)

    // Сдвиг фаз дрейфа: пятна проходят сквозь друг друга, а не вращаются как одно целое.
    val secondaryAngle = angle + 2f * PI.toFloat() / 3f
    val secondaryCenter = center + Offset(w * 0.22f, 0f) +
        Offset(driftX * 0.8f * cos(secondaryAngle), driftY * sin(secondaryAngle))
    val secondaryRadius = base * 0.45f * blobScale
    drawAuroraBlob(center = secondaryCenter, radius = secondaryRadius, color = colors.secondary, alpha = 0.7f)

    val warmAngle = angle + 4f * PI.toFloat() / 3f
    val warmCenter = center + Offset(0f, h * 0.18f) +
        Offset(driftX * cos(warmAngle), driftY * 0.7f * sin(warmAngle))
    val warmRadius = base * 0.35f * blobScale
    drawAuroraBlob(center = warmCenter, radius = warmRadius, color = colors.warm, alpha = 0.7f)
}

/**
 * Рисует одно мягкое пятно: радиальный градиент от [color] с [alpha] к прозрачному.
 */
private fun DrawScope.drawAuroraBlob(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/**
 * Цвета «авроры» — мягких цветных пятен, которые дрейфуют внутри жидкого стекла.
 * Именно они делают эффект стекла видимым даже на однородном фоне: блюр и рефракция
 * получают «контент», который можно размывать и преломлять.
 */
@Immutable
data class AuroraColors(val main: Color, val secondary: Color, val warm: Color) {
    companion object {
        @Composable
        fun fromTheme(): AuroraColors {
            val scheme = QaTheme.colorScheme
            return AuroraColors(
                main = scheme.auroraMain,
                secondary = scheme.auroraSecondary,
                warm = scheme.auroraWarm,
            )
        }
    }
}
