package ru.vladislavsumin.core.ui.liquidGlass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val colors = QaTheme.colorScheme
    return if (backdrop != null) {
        drawBackdrop(
            backdrop = backdrop,
            shape = { shapes.small },
            effects = {
                vibrancy()
                blur(6f.dp.toPx())
                lens(6f.dp.toPx(), 32f.dp.toPx())
            },
            onDrawSurface = {
                //drawRect(colors.background3.copy(alpha = .85f))

                drawRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.White.copy(alpha = 0.12f),
                        0.15f to Color.White.copy(alpha = 0.04f),
                        0.40f to Color.Transparent,
                    ),
                )

                drawRect(
                    brush = Brush.linearGradient(
                        0.00f to Color.White.copy(alpha = 0.06f),
                        0.25f to Color.White.copy(alpha = 0.02f),
                        0.50f to Color.Transparent,
                    ),
                )

                drawRect(
                    brush = Brush.verticalGradient(
                        0.70f to Color.Transparent,
                        0.90f to Color.White.copy(alpha = 0.02f),
                        1.00f to Color.White.copy(alpha = 0.06f),
                    ),
                )
            }
        )
    } else {
        this
    }
}
