package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.LoggerManager

object TestLogger {
    private var isInitialized = false
    fun init() {
        if (!isInitialized) {
            isInitialized = true
            LoggerManager.init(externalLoggerFactory = {
                object : ExternalLogger {
                    override fun log(level: LogLevel, msg: String) = Unit
                    override fun log(level: LogLevel, throwable: Throwable, msg: String) = Unit
                }
            })
        }
    }
}
