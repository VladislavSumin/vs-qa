package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.BinaryFlowLogParser
import ru.vladislavsumin.feature.logParser.domain.FileLogParser
import ru.vladislavsumin.feature.logParser.domain.LogParserProvider
import ru.vladislavsumin.feature.logParser.domain.StringFlowLogParser
import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser
import ru.vladislavsumin.feature.logParser.logcat.domain.LogcatBinaryFlowLogParser
import ru.vladislavsumin.feature.logParser.logcat.domain.LogcatLongStringFlowLogParser

class AnimeLogParserProvider : LogParserProvider {
    override val name: String = "Anime"

    override fun getFileLogParser(): FileLogParser = AnimeFileLogParser()
    override fun getStringFlowLogParser(): StringFlowLogParser = LogcatLongStringFlowLogParser()
    override fun getBinaryFlowLogParser(): BinaryFlowLogParser = LogcatBinaryFlowLogParser()
    override fun getRunIdParser(): RunIdParser = AnimeRunIdParser()
}
