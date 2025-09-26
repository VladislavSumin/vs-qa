package ru.vladislavsumin.qa.feature.rootScreen

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactory
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactoryImpl
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenFactory
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootViewModelFactory

fun Modules.featureRootScreen() = DI.Module("feature-rootScreen") {
    bindGenericNavigation { NavigationRegistrarImpl(i()) }
    bindSingleton {
        val vmf = RootViewModelFactory(i())
        RootScreenFactory(vmf, i(), i())
    }
    bindSingleton<RootScreenComponentFactory> { RootScreenComponentFactoryImpl(i()) }
}
