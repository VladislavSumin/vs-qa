package ru.vladislavsumin.feature.logsDashboard

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.feature.logsDashboard.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.feature.logsDashboard.ui.screen.logsDashboard.LogsDashboardScreenFactory
import ru.vladislavsumin.feature.logsDashboard.ui.screen.logsDashboard.LogsDashboardScreenFactoryImpl

fun Modules.featureLogsDashboard() = DI.Module("feature-logsDashboard") {
    bindSingleton<LogsDashboardScreenFactory> { LogsDashboardScreenFactoryImpl() }
    bindGenericNavigation { NavigationRegistrarImpl() }
}
