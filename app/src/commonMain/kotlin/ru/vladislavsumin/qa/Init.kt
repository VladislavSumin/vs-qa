package ru.vladislavsumin.qa

import org.kodein.di.DI
import org.kodein.di.DirectDI
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.platform.initDefault
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager

fun preInit(
    globalHotkeyManager: GlobalHotkeyManager,
    platformModule: DI.Module? = null,
): DirectDI {
    LoggerManager.initDefault()
    MainLogger.i("preInit()")
    // TODO сделать 2 ступени инициализации.
    return createDi(platformModule, globalHotkeyManager)
}
