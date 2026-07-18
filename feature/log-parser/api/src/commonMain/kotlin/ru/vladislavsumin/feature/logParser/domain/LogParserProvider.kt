package ru.vladislavsumin.feature.logParser.domain

import ru.vladislavsumin.feature.logParser.domain.runId.RunIdParser

interface LogParserProvider {
    val name: String
    fun getFileLogParser(): FileLogParser
    fun getStringFlowLogParser(): StringFlowLogParser
    fun getBinaryFlowLogParser(): BinaryFlowLogParser
    fun getRunIdParser(): RunIdParser? = null
}
