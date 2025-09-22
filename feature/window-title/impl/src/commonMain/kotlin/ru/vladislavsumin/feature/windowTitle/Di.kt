package ru.vladislavsumin.feature.windowTitle

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractorImpl

fun Modules.featureWidowTitle() = DI.Module("feature-windowTitle") {
    bindSingleton<WindowTitleInteractor> { WindowTitleInteractorImpl() }
}
