package ru.vladislavsumin.qa.feature.memoryIndicator

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator.MemoryIndicatorComponentFactory
import ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator.MemoryIndicatorComponentFactoryImpl

fun Modules.featureMemoryIndicator() = DI.Module("feature-memoryIndicator") {
    bindSingleton<MemoryIndicatorComponentFactory> { MemoryIndicatorComponentFactoryImpl() }
}
