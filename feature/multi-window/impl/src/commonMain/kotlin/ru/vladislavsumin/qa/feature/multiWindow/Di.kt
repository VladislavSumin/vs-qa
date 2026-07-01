package ru.vladislavsumin.qa.feature.multiWindow

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.multiWindow.ui.component.multiWindowRootScreen.MultiWindowRootScreenComponentFactory
import ru.vladislavsumin.qa.feature.multiWindow.ui.component.multiWindowRootScreen.MultiWindowRootScreenComponentFactoryImpl
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot.MultiWindowRootScreenFactory
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenFactory

fun Modules.featureMultiWindow() = DI.Module("feature-multiWindow") {
    bindGenericNavigation {
        val multiWindowRootScreenFactory = MultiWindowRootScreenFactory()
        val windowScreenFactory = WindowScreenFactory(i())
        NavigationRegistrarImpl(multiWindowRootScreenFactory, windowScreenFactory)
    }
    bindSingleton<MultiWindowRootScreenComponentFactory> { MultiWindowRootScreenComponentFactoryImpl(i(), i()) }
}
