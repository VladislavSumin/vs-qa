package ru.vladislavsumin.feature.logViewer

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.feature.logViewer.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactoryImpl
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerViewModelFactory

fun Modules.featureLogViewer() = DI.Module("feature-logViewer") {
    bindGenericNavigation { NavigationRegistrarImpl() }

    bindSingleton<LogViewerScreenFactory> {
        val vmf = LogViewerViewModelFactory(i(), i())
        LogViewerScreenFactoryImpl(vmf)
    }
}
