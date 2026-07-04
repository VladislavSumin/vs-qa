package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.ScreenParams

@Serializable
data class WindowScreenParams(val id: String) : ScreenParams
