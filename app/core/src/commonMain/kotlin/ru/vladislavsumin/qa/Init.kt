package ru.vladislavsumin.qa

import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import org.kodein.di.DI
import org.kodein.di.DirectDI
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.platform.LogPath
import ru.vladislavsumin.core.logger.platform.initDefault

fun preInit(platformModule: DI.Module? = null, stdout: Boolean = true): DirectDI {
    LoggerManager.initDefault(logPath = LogPath.UserHome(".vs-qa"), stdout = stdout)
    MainLogger.i("preInit()")

    ComposeStabilityAnalyzer.setEnabled(false)

    // TODO сделать 2 ступени инициализации.
    return createDi(platformModule)
}
