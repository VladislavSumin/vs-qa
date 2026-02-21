package ru.vladislavsumin.qa.feature.homeScreen

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.homeScreen.ui.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenFactoryImpl
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenViewModelFactory

fun Modules.featureHomeScreen() = DI.Module("feature-homeScreen") {
    bindSingleton {
        val viewModelFactory = HomeScreenViewModelFactory(i())
        HomeScreenFactoryImpl(viewModelFactory, i(), i(), i())
    }
    bindGenericNavigation { NavigationRegistrarImpl() }
}
