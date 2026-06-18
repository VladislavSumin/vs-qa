package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

interface AdbDeviceListComponentFactory {
    fun create(onDeviceClick: (deviceName: String) -> Unit, context: ComponentContext): ComposeComponent
}
