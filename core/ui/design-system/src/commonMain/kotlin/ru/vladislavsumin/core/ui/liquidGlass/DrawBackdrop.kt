package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

/**
 * Рисует эффект жидкого стекла (блик по краю, тень, блюр и рефракция содержимого) под поверхностью.
 *
 * Если [backdrop] == null (стекло выключено) — модификатор не делает ничего, и поверхность
 * отрисовывается обычным способом.
 *
 * @param backdrop слой, из которого берётся содержимое стекла (см. [vsRememberLayerBackdrop]).
 * @param shape форма стеклянной поверхности.
 * @param aurora рисовать ли анимированную «аврору» внутри стекла. Она нужна на однородном фоне,
 *   где реальному блюру нечего размывать: пятна дают цвет и движение, видимые сквозь стекло.
 * @param effects цепочка эффектов (блюр, рефракция, вибрация цвета).
 * @param highlight подсветка края.
 * @param shadow внешняя тень.
 * @param innerShadow внутренняя тень (придаёт «толщину» стеклу).
 * @param layerBlock трансформации слоя поверхности (например, «жидкая» деформация при нажатии).
 * @param onDrawSurface отрисовка поверх стекла (тинт, цвет поверхности).
 */
@Composable
fun Modifier.vsDrawBackdrop(
    backdrop: Backdrop?,
    shape: Shape = QaTheme.shapes.small,
    aurora: Boolean = false,
    effects: BackdropEffectScope.() -> Unit = {
        vibrancy()
        blur(4f.dp.toPx())
        lens(16f.dp.toPx(), 32f.dp.toPx(), chromaticAberration = true)
    },
    highlight: (() -> Highlight?)? = { Highlight() },
    shadow: (() -> Shadow?)? = { Shadow.Default },
    innerShadow: (() -> InnerShadow?)? = null,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    onDrawSurface: (DrawScope.() -> Unit)? = null,
): Modifier = if (backdrop != null) {
    val combinedBackdrop = if (aurora) {
        rememberCombinedBackdrop(backdrop, rememberAuroraBackdrop())
    } else {
        backdrop
    }
    drawBackdrop(
        backdrop = combinedBackdrop,
        shape = { shape },
        effects = effects,
        highlight = highlight,
        shadow = shadow,
        innerShadow = innerShadow,
        layerBlock = layerBlock,
        onDrawSurface = onDrawSurface,
    )
} else {
    this
}
