package ru.vladislavsumin.feature.logParser.domain

interface LogParserProvider {
    val name: String
    fun getFileLogParser(): FileLogParser
    fun getStringFlowLogParser(): StringFlowLogParser
    fun getBinaryFlowLogParser(): BinaryFlowLogParser
}
