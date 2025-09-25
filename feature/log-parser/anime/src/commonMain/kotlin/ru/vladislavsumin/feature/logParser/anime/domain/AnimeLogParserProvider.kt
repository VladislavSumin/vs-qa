package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogParser
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser

class AnimeLogParserProvider : LogParserProvider {
    override val name: String = "Anime"

    override fun getLogParser(): LogParser = AnimeLogParser()
    override fun getRunIdParser(): RunIdParser = AnimeRunIdParser()
}
