package ru.vladislavsumin.qa.feature.tabs

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabsComponentFactory
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabsComponentFactoryImpl

fun Modules.featureTabs() = DI.Module("feature-tabs") {
    bindSingleton<TabsComponentFactory> { TabsComponentFactoryImpl(i()) }
}
