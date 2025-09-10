package ru.vladislavsumin.feature.logViewer.domain.logs

@Suppress("MagicNumber")
enum class LogLevel(val rawLevel: Int, val aliases: Set<String>) {
    FATAL(6, setOf("F", "FATAL")),
    ERROR(5, setOf("E", "ERROR")),
    WARN(4, setOf("W", "WARN")),
    INFO(3, setOf("I", "INFO")),
    DEBUG(2, setOf("D", "DEBUG")),
    VERBOSE(1, setOf("V", "VERBOSE", "T", "TRACE")),
    ;

    companion object {
        private val aliasMap = let {
            val map = mutableMapOf<String, LogLevel>()
            entries.forEach { level -> level.aliases.forEach { alias -> map[alias] = level } }
            map
        }

        fun fromAlias(alias: String): LogLevel? = aliasMap[alias.uppercase()]
    }
}
