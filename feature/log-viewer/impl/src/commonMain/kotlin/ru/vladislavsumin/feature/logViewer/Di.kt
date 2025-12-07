package ru.vladislavsumin.feature.logViewer

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.feature.logViewer.domain.SavedFiltersRepository
import ru.vladislavsumin.feature.logViewer.domain.SavedFiltersRepositoryImpl
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarViewModelFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactory
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenFactoryImpl
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerViewModelFactory

fun Modules.featureLogViewer() = DI.Module("feature-logViewer") {
    bindSingleton<SavedFiltersRepository> { SavedFiltersRepositoryImpl(i()) }

    bindGenericNavigation { NavigationRegistrarImpl() }

    bindSingleton<FilterBarComponentFactory> {
        val vmf = FilterBarViewModelFactory(i(), i())
        FilterBarComponentFactory(vmf)
    }

    bindSingleton<LogViewerScreenFactory> {
        val vmf = LogViewerViewModelFactory(i(), i(), i())
        LogViewerScreenFactoryImpl(vmf, i())
    }
}
