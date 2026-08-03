package ru.vladislavsumin.core.ui.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import java.awt.MouseInfo

@Composable
actual fun rememberGlobalPointerPosition(): State<Offset?> {
    val density = LocalDensity.current.density
    val state = remember { mutableStateOf<Offset?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            state.value = runCatching { MouseInfo.getPointerInfo() }
                .getOrNull()
                ?.location
                // AWT возвращает координаты в точках, а localToScreen — в Compose-пикселях;
                // умножаем на density, чтобы обе стороны были в одном пространстве.
                ?.let { Offset(it.x.toFloat() * density, it.y.toFloat() * density) }
            delay(16)
        }
    }
    return state
}
