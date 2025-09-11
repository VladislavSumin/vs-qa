package ru.vladislavsumin.qa.feature.bottomBar

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactoryImpl

fun Modules.featureBottomBar() = DI.Module("feature-bottomBar") {
    bindSingleton<BottomBarComponentFactory> { BottomBarComponentFactoryImpl(i()) }
}
