package ru.vladislavsumin.qa

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.direct
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.feature.logParser.anime.domain.featureAnimeLogParser
import ru.vladislavsumin.feature.logViewer.featureLogViewer
import ru.vladislavsumin.qa.feature.bottomBar.featureBottomBar
import ru.vladislavsumin.qa.feature.memoryIndicator.featureMemoryIndicator
import ru.vladislavsumin.qa.feature.rootScreen.featureRootScreen

fun createDi(): DirectDI = DI {
    importOnce(Modules.featureBottomBar())
    importOnce(Modules.featureLogViewer())
    importOnce(Modules.featureMemoryIndicator())
    importOnce(Modules.featureRootScreen())

    importOnce(Modules.featureAnimeLogParser())
}.direct
