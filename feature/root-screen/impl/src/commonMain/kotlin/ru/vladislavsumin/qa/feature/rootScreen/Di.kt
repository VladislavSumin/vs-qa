package ru.vladislavsumin.qa.feature.rootScreen

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactory
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactoryImpl

fun Modules.featureRootScreen() = DI.Module("feature-rootScreen") {
    bindSingleton<RootScreenComponentFactory> { RootScreenComponentFactoryImpl(i(), i()) }
}
