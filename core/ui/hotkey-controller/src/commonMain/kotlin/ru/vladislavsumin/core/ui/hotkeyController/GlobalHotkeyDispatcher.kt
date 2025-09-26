package ru.vladislavsumin.core.ui.hotkeyController

import androidx.compose.ui.input.key.KeyEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CopyOnWriteArrayList

class GlobalHotkeyDispatcher : GlobalHotkeyManager {

    private val subscriptions = CopyOnWriteArrayList<HotkeyController>()

    override suspend fun subscribe(vararg hotkeys: Pair<Hotkey, () -> Boolean>): Nothing =
        suspendCancellableCoroutine {
            val controller = HotkeyController(hotkeys = hotkeys)
            subscriptions.add(controller)
            it.invokeOnCancellation { subscriptions.remove(controller) }
        }

    fun onKeyEvent(event: KeyEvent): Boolean = subscriptions.any { it(event) }
}
