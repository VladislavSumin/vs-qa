package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.FileLogParser
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.StringFlowLogParser
import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser

class AnimeLogParserProvider : LogParserProvider {
    override val name: String = "Anime"

    override fun getFileLogParser(): FileLogParser = AnimeFileLogParser()
    override fun getStringFlowLogParser(): StringFlowLogParser = AnimeFileLogParser()
    override fun getRunIdParser(): RunIdParser = AnimeRunIdParser()
}
