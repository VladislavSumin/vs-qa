package ru.vladislavsumin.qa.feature.settings

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.settings.domain.SettingsInteractor
import ru.vladislavsumin.qa.feature.settings.domain.SettingsInteractorImpl
import ru.vladislavsumin.qa.feature.settings.domain.SettingsInteractorInternal
import ru.vladislavsumin.qa.feature.settings.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.settings.ui.screen.settings.SettingsScreenFactory
import ru.vladislavsumin.qa.feature.settings.ui.screen.settings.SettingsScreenViewModelFactory

fun Modules.featureSettings() = DI.Module("feature-settings") {
    bindSingleton<SettingsInteractorInternal> { SettingsInteractorImpl(i(), i()) }
    bindSingleton<SettingsInteractor> { i<SettingsInteractorInternal>() }

    bindGenericNavigation {
        val viewModelFactory = SettingsScreenViewModelFactory(i())
        NavigationRegistrarImpl(SettingsScreenFactory(viewModelFactory))
    }
}
