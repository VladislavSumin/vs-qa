package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.LoggerManager

// TODO вынести в базовые методы.
object TestLogger {
    private var isInitialized = false
    fun init() {
        if (!isInitialized) {
            isInitialized = true
            LoggerManager.init(externalLoggerFactory = {
                object : ExternalLogger {
                    override fun log(level: LogLevel, msg: String) {
                        // no_op
                    }

                    override fun log(
                        level: LogLevel,
                        throwable: Throwable,
                        msg: String,
                    ) {
                        // no_op
                    }
                }
            })
        }
    }
}
