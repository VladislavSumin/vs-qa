package ru.vladislavsumin.core.ui.button

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.liquidGlass.pressRefraction
import ru.vladislavsumin.core.ui.liquidGlass.rememberRippleSource
import ru.vladislavsumin.core.ui.liquidGlass.rememberWavinessPhase
import ru.vladislavsumin.core.ui.liquidGlass.ripples
import ru.vladislavsumin.core.ui.liquidGlass.vsDrawBackdrop
import ru.vladislavsumin.core.ui.liquidGlass.waviness
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Кнопка в стиле «жидкого стекла».
 *
 * При [backdrop] != null поверхность рисуется как стекло: блюр + рефракция содержимого слоя
 * и анимированной «авроры» (см. [ru.vladislavsumin.core.ui.liquidGlass.vsDrawBackdrop]),
 * подсветка края и «жидкая» деформация при нажатии. При [backdrop] == null (стекло выключено)
 * кнопка выглядит как аккуратная градиентная поверхность с обводкой и тенью.
 *
 * @param tint акцентная полупрозрачная вуаль поверх стекла (аврора просвечивает сквозь неё).
 * @param tintAlpha непрозрачность вуали [tint].
 * @param surfaceColor цвет «молочной» поверхности стекла поверх [tint].
 * @param shape форма кнопки, по умолчанию — капсула.
 * @param rippleIntervalMs базовый интервал между каплями «волн на воде» в миллисекундах
 *   (с рандомизацией ±50% от значения); капли падают только когда курсор над кнопкой
 *   и сдвинулся не меньше чем на [rippleMoveDelta].
 * @param rippleMoveDelta минимальное смещение курсора (в Dp) для новой капли — капли
 *   «прилипают» к движению курсора и не сыплются на месте.
 */
@Composable
fun QaLiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    tintAlpha: Float = 0.15f,
    surfaceColor: Color = Color.Unspecified,
    shape: Shape = Capsule(),
    rippleIntervalMs: Long = 60,
    rippleMoveDelta: Dp = 12.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) { InteractiveHighlight(animationScope) }
    val wavinessPhase = rememberWavinessPhase()
    val rippleSource = rememberRippleSource()

    // Наведение: курсор отслеживается даже за границами кнопки (через глобальный трекер),
    // интенсивность плавно нарастает и убывает с расстоянием до кнопки.
    val hoverProgress = remember { Animatable(0f) }
    var pointerPosition by remember { mutableStateOf(Offset.Zero) }
    var buttonLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    val globalPointerPosition = rememberGlobalPointerPosition()
    // Глобальный курсор в экранных координатах → локальные координаты кнопки (может выходить за границы).
    val localCursor = globalPointerPosition.value
        ?.takeIf { it.isSpecified }
        ?.let { mouse ->
            buttonLayoutCoordinates
                ?.takeIf { it.isAttached }
                ?.localToScreen(Offset.Zero)
                ?.takeIf { it.isSpecified }
                ?.let { mouse - it }
        }
    val maxHoverDistance = with(LocalDensity.current) { 120.dp.toPx() }
    val sizeKnown = buttonSize.width > 0 && buttonSize.height > 0
    val hoverPresence = sizeKnown && localCursor != null &&
        distanceToRect(localCursor, buttonSize.width.toFloat(), buttonSize.height.toFloat()) <= maxHoverDistance
    // Волны на воде: капли спавнятся, пока курсор над кнопкой и сдвинулся не меньше чем
    // на rippleMoveDelta с момента последней капли; частота ограничена rippleIntervalMs
    // (±50% рандомизации) — капли «прилипают» к движению, а не сыплются на месте.
    val cursorOverButton = sizeKnown && localCursor != null &&
        distanceToRect(localCursor, buttonSize.width.toFloat(), buttonSize.height.toFloat()) == 0f
    val rippleMoveDeltaPx = with(LocalDensity.current) { rippleMoveDelta.toPx() }
    LaunchedEffect(cursorOverButton) {
        var lastDropPosition: Offset? = null
        while (cursorOverButton) {
            val position = pointerPosition
            val movedEnough = lastDropPosition == null ||
                (position - lastDropPosition).getDistance() >= rippleMoveDeltaPx
            if (movedEnough) {
                val jitter = Offset(
                    x = (Random.nextFloat() - 0.5f) * 8f,
                    y = (Random.nextFloat() - 0.5f) * 8f,
                )
                rippleSource.spawn(position + jitter)
                lastDropPosition = position
            }
            delay(Random.nextLong(rippleIntervalMs / 2, rippleIntervalMs * 3 / 2 + 1))
        }
    }
    LaunchedEffect(hoverPresence) {
        hoverProgress.animateTo(
            targetValue = if (hoverPresence) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
        )
    }

    val surfaceModifier = if (backdrop != null) {
        Modifier.vsDrawBackdrop(
            backdrop = backdrop,
            shape = shape,
            aurora = false,
            effects = {
                vibrancy()
                blur(3.3f.dp.toPx())
                lens(6f.dp.toPx(), 24f.dp.toPx())

                // «Кривое стекло»: лёгкие искажения по всей поверхности, дрейф ведёт фаза.
                waviness(
                    amount = size.minDimension * 0.05f,
                    frequency = 1.5f,
                    phase = wavinessPhase.value,
                )

                // «Продавливание» стекла: чтение Animatable-стейтов внутри эффектов
                // наблюдается библиотекой, поэтому лунка следует за анимациями нажатия
                // и плавного наведения. Позиция — глобальный курсор (работает и за
                // границами кнопки), на Android — локальный трекер.
                val press = interactiveHighlight.pressProgress
                val bumpPosition = localCursor ?: pointerPosition
                val dist = distanceToRect(bumpPosition, size.width, size.height)
                val hoverFactor = (1f - dist / maxHoverDistance).coerceIn(0f, 1f)
                pressRefraction(
                    amount = max(press, hoverProgress.value * 0.5f * hoverFactor) * 2,
                    touchPos = bumpPosition,
                    radius = min(size.maxDimension * 0.25f, size.minDimension * 1.5f),
                    strength = size.minDimension * 0.35f,
                )

                // «Волны на воде»: мелкая выраженная рябь — частые гребни (~0.35·minDim),
                // амплитуда ~0.16·minDim, разбег ~30px/с, локальная огибающая.
                ripples(
                    source = rippleSource,
                    amount = size.minDimension * 0.16f,
                    wavelength = size.minDimension * 0.35f,
                    speed = 11f,
                    decay = 0.9f,
                    falloff = size.minDimension * 1f,
                )
            },
            layerBlock = {
                // Равномерное «набухание» поверхности при нажатии
                val progress = interactiveHighlight.pressProgress
                val scale = lerp(1f, 1f + 2f.dp.toPx() / size.height, progress)
                scaleX = scale
                scaleY = scale
            },
            onDrawSurface = {
                if (tint.isSpecified) {
                    drawRect(tint.copy(alpha = tintAlpha))
                }
                if (surfaceColor.isSpecified) {
                    drawRect(surfaceColor)
                }
            },
        )
    } else {
        val colors = QaTheme.colorScheme
        Modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.35f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(listOf(colors.background3, colors.background1)))
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    }

    Row(
        modifier
            .then(surfaceModifier)
            .onGloballyPositioned { coords ->
                buttonLayoutCoordinates = coords
                buttonSize = coords.size
            }
            .pointerInput(Unit) {
                // Локальный трекер — запасной источник позиции на платформах без
                // глобального курсора (Android/touch): Move-события приходят
                // и при наведении, и во время драга.
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter) {
                            pointerPosition = event.changes.first().position
                        }
                    }
                }
            }
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .then(interactiveHighlight.gestureModifier)
            .height(48.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

// Порт InteractiveHighlight из официального примера LiquidButton
// (https://github.com/Kyant0/AndroidLiquidGlass), лицензия Apache-2.0.
// Следит за нажатием: глубина нажатия для эффекта «продавливания» стекла
// и равномерного «набухания» поверхности.
private class InteractiveHighlight(val animationScope: CoroutineScope) {

    private val pressProgressAnimationSpec = spring(0.5f, 300f, 0.001f)

    private val pressProgressAnimation = Animatable(0f, 0.001f)
    val pressProgress: Float get() = pressProgressAnimation.value

    val gestureModifier: Modifier =
        Modifier.pointerInput(animationScope) {
            inspectDragGestures(
                onDragStart = {
                    animationScope.launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
                },
                onDragEnd = {
                    animationScope.launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
                },
                onDragCancel = {
                    animationScope.launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
                },
            )
        }

    // Аналог inspectDragGestures из старой версии Compose Foundation (удалена в новых версиях):
    // onDragStart срабатывает сразу на нажатие (без ожидания touch slop),
    // поэтому эффект нажатия работает и для обычного клика мышью.
    private suspend fun PointerInputScope.inspectDragGestures(
        onDragStart: (Offset) -> Unit = {},
        onDragEnd: () -> Unit = {},
        onDragCancel: () -> Unit = {},
        onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit = { _, _ -> },
    ) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            onDragStart(down.position)
            var moved = false
            drag(pointerId = down.id) { change ->
                onDrag(change, change.position - change.previousPosition)
                change.consume()
                moved = true
            }
            if (moved) onDragEnd() else onDragCancel()
        }
    }
}

/**
 * Расстояние от точки [p] до прямоугольника размера [w]x[h] (0 внутри прямоугольника).
 */
private fun distanceToRect(p: Offset, w: Float, h: Float): Float {
    val dx = max(0f, max(-p.x, p.x - w))
    val dy = max(0f, max(-p.y, p.y - h))
    return sqrt(dx * dx + dy * dy)
}
