package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogParser
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider

class AnimeLogParserProvider : LogParserProvider {
    override val name: String = "Anime"

    override fun getLogParser(): LogParser = AnimeLogParser()
}
