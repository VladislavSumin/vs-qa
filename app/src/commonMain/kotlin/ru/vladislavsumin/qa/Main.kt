package ru.vladislavsumin.qa

import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.platform.initDefault
import kotlin.system.exitProcess

fun main() {
    setExitOnUncaughtException()
    LoggerManager.initDefault()
    MainLogger.i("Initialization...")
}

fun setExitOnUncaughtException() {
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        handler?.uncaughtException(thread, throwable)
        throwable.printStackTrace()
        exitProcess(1)
    }
}