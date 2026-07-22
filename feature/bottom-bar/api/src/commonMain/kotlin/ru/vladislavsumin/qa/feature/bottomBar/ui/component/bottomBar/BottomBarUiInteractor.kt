package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import ru.vladislavsumin.core.ui.resources.ResourceString

interface BottomBarUiInteractor {

    /**
     * Показывает бесконечный прогресс бар с текстом [text] пока активна данная корутина.
     */
    suspend fun showProgressBar(text: ResourceString): Nothing

    /**
     * Устанавливает дополнительную информационную строку в нижнем баре.
     */
    fun setBottomBarText(text: ResourceString)
}
