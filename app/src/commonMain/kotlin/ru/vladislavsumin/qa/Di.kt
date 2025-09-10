package ru.vladislavsumin.qa

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.direct
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.feature.logViewer.featureLogViewer
import ru.vladislavsumin.qa.feature.memoryIndicator.featureMemoryIndicator

fun createDi(): DirectDI = DI {
    importOnce(Modules.featureLogViewer())
    importOnce(Modules.featureMemoryIndicator())
}.direct
