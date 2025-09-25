package ru.vladislavsumin.feature.logParser.domain

import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser

interface LogParserProvider {
    val name: String
    fun getLogParser(): LogParser
    fun getRunIdParser(): RunIdParser? = null
}
