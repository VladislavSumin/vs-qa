package ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

interface MemoryIndicatorComponentFactory {
    fun create(componentContext: ComponentContext): ComposeComponent
}
