package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Сбрасывает фокус с компонента по нажатию кнопки Esc.
 */
@Composable
fun Modifier.resetFocusOnEsc(): Modifier {
    val focusManager = LocalFocusManager.current
    val hotkeyController = remember(focusManager) {
        HotkeyController(
            KeyModifier.None + Key.Escape to {
                focusManager.clearFocus()
                true
            },
        )
    }
    return this.onKeyEvent(hotkeyController::invoke)
}
