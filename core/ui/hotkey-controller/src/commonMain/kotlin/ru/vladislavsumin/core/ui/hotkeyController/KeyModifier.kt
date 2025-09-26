package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed

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
