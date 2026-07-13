package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import org.jetbrains.compose.resources.StringResource

internal sealed interface DeviceParameter {
    val id: String
    val nameRes: StringResource
    val isLoading: Boolean

    data class Toggle(
        override val id: String,
        override val nameRes: StringResource,
        val isChecked: Boolean,
        override val isLoading: Boolean = false,
    ) : DeviceParameter
}
