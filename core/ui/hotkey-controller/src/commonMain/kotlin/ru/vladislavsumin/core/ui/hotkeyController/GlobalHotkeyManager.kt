package ru.vladislavsumin.core.ui.hotkeyController

/**
 * Позволяет подписаться на глобальные события нажатия клавиш.
 */
interface GlobalHotkeyManager {
    /**
     * Подписывается на набор [hotkeys] до тех пор пока активна вызывающая корутина.
     */
    suspend fun subscribe(vararg hotkeys: Pair<Hotkey, () -> Boolean>): Nothing
}
