package ru.vladislavsumin.feature.logParser.domain

interface LogParserProvider {
    val name: String
    fun getLogParser(): LogParser
}
