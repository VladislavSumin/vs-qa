package ru.vladislavsumin.feature.logParser.anime.domain

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider

fun Modules.featureAnimeLogParser() = DI.Module("feature-animeLogParser") {
    bindSingleton<LogParserProvider> { AnimeLogParserProvider() }
}
