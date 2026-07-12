package ru.vladislavsumin.qa.feature.settings

import org.kodein.di.DI
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.settings.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.settings.ui.screen.settings.SettingsScreenFactory

fun Modules.featureSettings() = DI.Module("feature-settings") {
    bindGenericNavigation {
        NavigationRegistrarImpl(SettingsScreenFactory())
    }
}
