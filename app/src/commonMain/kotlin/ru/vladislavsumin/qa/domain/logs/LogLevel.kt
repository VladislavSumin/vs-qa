package ru.vladislavsumin.qa.domain.logs

enum class LogLevel(val aliases: Set<String>) {
    FATAL(setOf("F", "FATAL")),
    ERROR(setOf("E", "ERROR")),
    WARN(setOf("W", "WARN")),
    INFO(setOf("I", "INFO")),
    DEBUG(setOf("D", "DEBUG")),
    VERBOSE(setOf("V", "VERBOSE", "T", "TRACE")),
    ;

    companion object {
        private val aliasMap = let {
            val map = mutableMapOf<String, LogLevel>()
            entries.forEach { level -> level.aliases.forEach { alias -> map[alias] = level } }
            map
        }

        fun fromAlias(alias: String): LogLevel? = aliasMap[alias]
    }
}
