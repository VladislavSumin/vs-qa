package ru.vladislavsumin.feature.logViewer

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactoryImpl

fun Modules.featureLogViewer() = DI.Module("feature-logViewer") {
    bindSingleton<LogViewerComponentFactory> { LogViewerComponentFactoryImpl() }
}
