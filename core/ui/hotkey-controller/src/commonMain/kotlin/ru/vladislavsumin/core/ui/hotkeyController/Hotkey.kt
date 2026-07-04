package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

class Hotkey internal constructor(private val modifier: KeyModifier, private val key: Key) {
    internal fun verify(event: KeyEvent): Boolean = modifier.verify(event) && key == event.key

    override fun toString(): String = "$modifier + $key"
}
