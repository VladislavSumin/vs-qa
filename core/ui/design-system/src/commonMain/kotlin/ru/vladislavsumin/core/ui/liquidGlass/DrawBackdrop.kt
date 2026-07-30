package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

@Composable
fun Modifier.vsDrawBackdrop(backdrop: Backdrop?): Modifier {
    val shapes = QaTheme.shapes
    return if (backdrop != null) {
        drawBackdrop(
            backdrop = backdrop,
            shape = { shapes.small },
            effects = {
                vibrancy()
                blur(4f.dp.toPx())
                lens(16f.dp.toPx(), 32f.dp.toPx())
            },
        )
    } else {
        this
    }
}
