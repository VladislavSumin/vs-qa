package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

internal sealed interface DeviceParameter {
    val id: String
    val name: String
    val isLoading: Boolean

    data class Toggle(
        override val id: String,
        override val name: String,
        val isChecked: Boolean,
        override val isLoading: Boolean = false,
    ) : DeviceParameter
}
