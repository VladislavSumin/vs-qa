package ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

internal class MemoryIndicatorComponentFactoryImpl : MemoryIndicatorComponentFactory {
    override fun create(componentContext: ComponentContext): ComposeComponent {
        return MemoryIndicatorComponent(componentContext)
    }
}
