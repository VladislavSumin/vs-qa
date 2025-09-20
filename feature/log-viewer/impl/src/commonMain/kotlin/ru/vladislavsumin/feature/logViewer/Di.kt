package ru.vladislavsumin.feature.logViewer

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactoryImpl
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerViewModelFactory

fun Modules.featureLogViewer() = DI.Module("feature-logViewer") {
    bindSingleton<LogViewerComponentFactory> {
        val vmf = LogViewerViewModelFactory(i())
        LogViewerComponentFactoryImpl(vmf)
    }
}
