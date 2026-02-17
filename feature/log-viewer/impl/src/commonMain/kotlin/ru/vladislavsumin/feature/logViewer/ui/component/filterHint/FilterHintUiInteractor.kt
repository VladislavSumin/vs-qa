package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import kotlinx.coroutines.channels.ReceiveChannel

internal interface FilterHintUiInteractor {
    val events: ReceiveChannel<Event>

    /**
     * Запрашивает показ подсказки.
     */
    fun requestShow()

    sealed interface Event {
        /**
         * Добавить текст после текущей позиции курсора и подвинуть курсор на длину этого текста.
         */
        data class AppendText(val text: String) : Event
    }
}
