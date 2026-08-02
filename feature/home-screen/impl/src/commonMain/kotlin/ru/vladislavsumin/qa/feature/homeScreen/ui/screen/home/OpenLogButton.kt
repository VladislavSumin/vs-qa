package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.liquidGlass.vsDrawBackdrop
import ru.vladislavsumin.core.ui.liquidGlass.vsLayerBackdrop
import ru.vladislavsumin.core.ui.liquidGlass.vsRememberLayerBackdrop

@Composable
internal fun OpenLogButton(modifier: Modifier = Modifier) {
    val colors = QaTheme.colorScheme
    val colorA = colors.background2.copy(alpha = 0.8f)
    val colorB = colors.backgroundAccent1.copy(alpha = 0.35f)

    val backdrop = vsRememberLayerBackdrop()

    val transition = rememberInfiniteTransition(label = "noise")
    val offset1X by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000), repeatMode = RepeatMode.Reverse),
        label = "offset1X",
    )
    val offset1Y by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), repeatMode = RepeatMode.Reverse),
        label = "offset1Y",
    )
    val offset2X by transition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3500), repeatMode = RepeatMode.Reverse),
        label = "offset2X",
    )
    val offset2Y by transition.animateFloat(
        initialValue = 0.5f, targetValue = -0.5f,
        animationSpec = infiniteRepeatable(tween(5000), repeatMode = RepeatMode.Reverse),
        label = "offset2Y",
    )
    val offset3X by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(4500), repeatMode = RepeatMode.Reverse),
        label = "offset3X",
    )
    val offset3Y by transition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(3800), repeatMode = RepeatMode.Reverse),
        label = "offset3Y",
    )

    Box(modifier) {
        Box(
            Modifier.matchParentSize()
                .alpha(0f)
                .vsLayerBackdrop(backdrop)
        ) {
            Box(
                Modifier.matchParentSize()
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        val r = kotlin.math.max(w, h)

                        val c1 = offset1X * w
                        val d1 = offset1Y * h
                        val c2 = offset2X * w
                        val d2 = offset2Y * h
                        val c3 = offset3X * w
                        val d3 = offset3Y * h

                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to colorA,
                                1.0f to Color.Transparent,
                                center = Offset(c1, d1),
                                radius = r,
                            ),
                            radius = r,
                            center = Offset(w / 2, h / 2),
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to colorB,
                                1.0f to Color.Transparent,
                                center = Offset(c2, d2),
                                radius = r,
                            ),
                            radius = r,
                            center = Offset(w / 2, h / 2),
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to colorA,
                                0.5f to colorB,
                                1.0f to Color.Transparent,
                                center = Offset(c3, d3),
                                radius = r,
                            ),
                            radius = r,
                            center = Offset(w / 2, h / 2),
                        )
                    }
            )
            Box(Modifier.padding(32.dp).background(Color.Red).matchParentSize())
        }

        Box(Modifier
//            .padding(start = 40.dp)
            .vsDrawBackdrop(backdrop).width(200.dp).height(48.dp))
    }
}