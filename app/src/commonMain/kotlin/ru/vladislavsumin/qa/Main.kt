package ru.vladislavsumin.qa

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.kodein.di.instance
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.platform.initDefault
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
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
    val logViewerComponentFactory = di.instance<LogViewerComponentFactory>()
    val bottomBarComponentFactory = di.instance<BottomBarComponentFactory>()

    // Создаем рутовый Decompose lifecycle.
    val lifecycle = LifecycleRegistry()

    val context = runOnUiThread {
        DefaultComponentContext(lifecycle)
    }

    val bottomBarComponent = runOnUiThread {
        bottomBarComponentFactory.create(context.childContext("bottom-bar"))
    }

    val logViewerComponent = runOnUiThread {
        logViewerComponentFactory.create(
            logPath,
            mappingPath,
            bottomBarComponent.bottomBarUiInteractor,
            context.childContext("log-viewer"),
        )
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
                Surface {
                    Column {
                        logViewerComponent.Render(Modifier.weight(1f))
                        bottomBarComponent.Render(Modifier)
                    }
                }
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
