package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed

sealed class KeyModifier {
    operator fun plus(key: Key) = Hotkey(this, key)
    operator fun plus(modifier: KeyModifier): KeyModifier = Composite(this, modifier)

    internal abstract val mask: Int

    internal fun verify(event: KeyEvent): Boolean = mask == getKeyEventMask(event)

    data object None : KeyModifier() {
        override val mask = NONE
    }

    data object Shift : KeyModifier() {
        override val mask = SHIFT
    }

    data object Command : KeyModifier() {
        override val mask = COMMAND
    }

    data object Control : KeyModifier() {
        override val mask = CONTROL
    }

    internal data class Composite(
        val left: KeyModifier,
        val right: KeyModifier,
    ) : KeyModifier() {
        override val mask = left.mask or right.mask
    }

    internal companion object {
        fun getKeyEventMask(event: KeyEvent): Int {
            var mask = 0
            if (event.isMetaPressed) mask = mask or COMMAND
            if (event.isShiftPressed) mask = mask or SHIFT
            if (event.isCtrlPressed) mask = mask or CONTROL
            return mask
        }

        private const val NONE = 0
        private const val COMMAND = 1 shl 0
        private const val SHIFT = 1 shl 1
        private const val CONTROL = 1 shl 2
    }
}
