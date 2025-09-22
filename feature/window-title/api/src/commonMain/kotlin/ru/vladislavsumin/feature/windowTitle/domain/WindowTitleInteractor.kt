package ru.vladislavsumin.feature.windowTitle.domain

import kotlinx.coroutines.flow.StateFlow

interface WindowTitleInteractor {
    val windowTitleExtension: StateFlow<String?>

    /**
     * Устанавливает дополнительную информацию в название окна.
     */
    fun setWindowTitleExtension(data: String?)
}
