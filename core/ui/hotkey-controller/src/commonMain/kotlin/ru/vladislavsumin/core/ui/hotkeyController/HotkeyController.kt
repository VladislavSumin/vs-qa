package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
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
            if (hotkey.modifier.verify(event) && hotkey.key == event.key && action()) {
                return true
            }
        }

        return false
    }
}

sealed class KeyModifier {
    operator fun plus(key: Key) = Hotkey(this, key)
    operator fun plus(modifier: KeyModifier): KeyModifier = Composite(this, modifier)

    internal abstract fun verify(event: KeyEvent): Boolean

    data object None : KeyModifier() {
        override fun verify(event: KeyEvent): Boolean = true
    }

    data object Shift : KeyModifier() {
        override fun verify(event: KeyEvent): Boolean = event.isShiftPressed
    }

    data object Command : KeyModifier() {
        override fun verify(event: KeyEvent): Boolean = event.isMetaPressed
    }

    internal data class Composite(
        val left: KeyModifier,
        val right: KeyModifier,
    ) : KeyModifier() {
        override fun verify(event: KeyEvent): Boolean = left.verify(event) && right.verify(event)
    }
}

class Hotkey internal constructor(
    val modifier: KeyModifier,
    val key: Key,
)
