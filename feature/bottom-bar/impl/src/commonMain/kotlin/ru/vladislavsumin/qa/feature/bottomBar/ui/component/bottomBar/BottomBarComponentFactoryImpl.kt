package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator.MemoryIndicatorComponentFactory

internal class BottomBarComponentFactoryImpl(
    private val memoryIndicatorComponentFactory: MemoryIndicatorComponentFactory,
) : BottomBarComponentFactory {
    override fun create(context: ComponentContext): BottomBarComponent {
        return BottomBarComponentImpl(memoryIndicatorComponentFactory, context)
    }
}
