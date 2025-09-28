package ru.vladislavsumin.qa

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.kodein.di.instance
import ru.vladislavsumin.core.decompose.compose.runOnUiThread
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.platform.initDefault
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyDispatcher
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactory
import kotlin.io.path.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    setExitOnUncaughtException()

    LoggerManager.initDefault()
    MainLogger.i("Initialization...")

    val logPath = if (args.isNotEmpty()) Path(args[0]) else null
    val mappingPath = if (args.size > 1) Path(args[1]) else null

    val hotkeyDispatcher = GlobalHotkeyDispatcher()
    val di = createDi(hotkeyDispatcher)

    // Создаем рутовый Decompose lifecycle.
    val lifecycle = LifecycleRegistry()

    val rootScreenComponent = runOnUiThread {
        val context = DefaultComponentContext(lifecycle)
        di.instance<RootScreenComponentFactory>().create(logPath, mappingPath, context)
    }

    val windowTitleInteractor = di.instance<WindowTitleInteractor>()

    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
        val windowTitleExtension by windowTitleInteractor.windowTitleExtension.collectAsState()
        val windowTitle = if (windowTitleExtension == null) "vs-qa" else "vs-qa: $windowTitleExtension"

        // Связываем рутовый Decompose lifecycle с жизненным циклом окна.
        LifecycleController(lifecycle, windowState)

        Window(
            title = windowTitle,
            onCloseRequest = ::exitApplication,
            state = windowState,
            onKeyEvent = hotkeyDispatcher::onKeyEvent,
        ) { rootScreenComponent.Render(Modifier) }
    }
}

fun setExitOnUncaughtException() {
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        handler?.uncaughtException(thread, throwable)
        throwable.printStackTrace()
        exitProcess(1)
    }
}
