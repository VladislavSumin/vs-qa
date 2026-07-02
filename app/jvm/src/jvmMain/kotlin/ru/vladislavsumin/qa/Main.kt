package ru.vladislavsumin.qa

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import io.sentry.kotlin.multiplatform.Sentry
import org.kodein.di.instance
import ru.vladislavsumin.core.decompose.compose.runOnUiThread
import ru.vladislavsumin.qa.feature.multiWindow.ui.component.multiWindowRootScreen.MultiWindowRootScreenComponentFactory
import kotlin.io.path.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    setExitOnUncaughtException()

    // TODO вынести в отдельный core модуль
    Sentry.init { options ->
        options.dsn = "https://ac13621e67953007e14fcfd5642531c4@o512687.ingest.us.sentry.io/4510488819793920"
    }

    val di = preInit()
    MainLogger.i("App version: ${BuildConfig.version}")

    val logPath = if (args.isNotEmpty()) Path(args[0]) else null
    val mappingPath = if (args.size > 1) Path(args[1]) else null

    // Создаем рутовый Decompose lifecycle.
    val lifecycle = LifecycleRegistry()

    val rootScreenComponent = runOnUiThread {
        val context = DefaultComponentContext(lifecycle)
        di.instance<MultiWindowRootScreenComponentFactory>().create(logPath, mappingPath, context)
    }

    application {
        rootScreenComponent.Render(Modifier)
        LaunchedEffect(lifecycle) {
            // Тут у нас нет контроля лайфсайкла так как нет окна, поэтому делаем всегда resume()
            // Окна сами управляют дочерним лайвсайклом.
            lifecycle.resume()
        }
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
