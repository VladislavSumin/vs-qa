package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

interface BottomBarUiInteractor {

    /**
     * Показывает бесконечный прогресс бар с текстом [text] пока активна данная корутина.
     */
    suspend fun showProgressBar(text: BottomBarText): Nothing

    /**
     * Устанавливает дополнительную информационную строку в нижнем баре.
     */
    fun setBottomBarText(text: BottomBarText)
}
