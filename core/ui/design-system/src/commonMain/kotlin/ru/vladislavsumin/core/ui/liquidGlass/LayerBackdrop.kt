package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

/**
 * Создаёт слой жидкого стекла, который записывает фон экрана.
 * Стеклянные поверхности (см. [vsDrawBackdrop]) сэмплируют этот слой и размывают его содержимое.
 *
 * @return слой, либо null если жидкое стекло выключено.
 */
@Composable
fun vsRememberLayerBackdrop(): LayerBackdrop? {
    if (!QaTheme.isLiquidGlass) return null
    val colors = QaTheme.colorScheme
    return rememberLayerBackdrop {
        drawRect(colors.background2)
        drawContent()
    }
}

fun Modifier.vsLayerBackdrop(backdrop: LayerBackdrop?): Modifier =
    if (backdrop != null) layerBackdrop(backdrop) else this
