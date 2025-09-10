package ru.vladislavsumin.qa

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.kodein.di.instance
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.platform.initDefault
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.qa.ui.component.logViewerComponent.LogViewerComponent
import javax.swing.SwingUtilities
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    setExitOnUncaughtException()

    LoggerManager.initDefault()
    MainLogger.i("Initialization...")

    val logPath = Path(args[0])
    val mappingPath = if (args.size > 1) Path(args[1]) else null

    val di = createDi()

    // Создаем рутовый Decompose lifecycle.
    val lifecycle = LifecycleRegistry()

    val component = runOnUiThread {
        val context = DefaultComponentContext(lifecycle)
        LogViewerComponent(logPath, mappingPath, di.instance(), context)
    }

    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Maximized,
        )

        // Связываем рутовый Decompose lifecycle с жизненным циклом окна.
        LifecycleController(lifecycle, windowState)

        Window(
            title = "vs-qa: ${logPath.name}",
            onCloseRequest = ::exitApplication,
            state = windowState,
        ) {
            QaTheme {
                component.Render(Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * See https://arkivanov.github.io/Decompose/getting-started/quick-start/
 * See https://github.com/arkivanov/Decompose/blob/master/sample/app-desktop/src/jvmMain/kotlin/com/arkivanov/sample/app/Utils.kt
 */
@Suppress("TooGenericExceptionCaught")
internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}

fun setExitOnUncaughtException() {
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        handler?.uncaughtException(thread, throwable)
        throwable.printStackTrace()
        exitProcess(1)
    }
}
