package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type

/**
 * Удобная обертка для обработки горячих клавиш.
 * ```kotlin
 * val hotkeyController = remember {
 *      HotkeyController(
 *          KeyModifier.Shift + Key.F to { action() },
 *      )
 * }
 * Modifier.onKeyEvent(hotkeyController::invoke)
 * ```
 */
class HotkeyController private constructor(private val hotkeys: List<Pair<Hotkey, () -> Boolean>>) {
    constructor(vararg hotkeys: Pair<Hotkey, () -> Boolean>) : this(hotkeys.toList())

    operator fun invoke(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        for ((hotkey, action) in hotkeys) {
            if (hotkey.verify(event) && action()) {
                return true
            }
        }

        return false
    }
}
