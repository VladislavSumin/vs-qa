package ru.vladislavsumin.feature.windowTitle.domain

import kotlinx.coroutines.flow.MutableStateFlow

class WindowTitleInteractorImpl : WindowTitleInteractor {
    override val windowTitleExtension: MutableStateFlow<String?> = MutableStateFlow(null)

    override fun setWindowTitleExtension(data: String?) {
        windowTitleExtension.value = data
    }
}
