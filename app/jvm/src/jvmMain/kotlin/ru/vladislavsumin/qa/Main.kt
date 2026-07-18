package ru.vladislavsumin.qa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.instance
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.decompose.compose.runOnUiThread
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.feature.mcp.domain.McpServer
import ru.vladislavsumin.qa.feature.multiWindow.ui.component.multiWindowRootScreen.MultiWindowRootScreenComponentFactory
import kotlin.io.path.Path
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

private const val STATE_DEBUG_ENABLED = false

fun main(args: Array<String>) {
    setExitOnUncaughtException()

    Sentry.init { options ->
        options.dsn = "https://ac13621e67953007e14fcfd5642531c4@o512687.ingest.us.sentry.io/4510488819793920"
    }

    if (args.firstOrNull() == "--mcp-server") {
        val di = preInit(stdout = false)
        MainLogger.i("App version: ${BuildConfig.version}")
        di.instance<McpServer>().start(BuildConfig.version)
        return
    }

    val di = preInit()
    MainLogger.i("App version: ${BuildConfig.version}")

    val logPath = if (args.isNotEmpty()) Path(args[0]) else null
    val mappingPath = if (args.size > 1) Path(args[1]) else null

    if (STATE_DEBUG_ENABLED) {
        val debugStateManager = DebugStateManager {
            di.instance<MultiWindowRootScreenComponentFactory>().create(logPath, mappingPath, it)
        }
        application {
            debugStateManager.Intercept { component, lifecycle ->
                component.Render(Modifier)
                LaunchedEffect(lifecycle) {
                    lifecycle.resume()
                }
            }
        }
    } else {
        val lifecycle = LifecycleRegistry()

        val rootScreenComponent = runOnUiThread {
            val context = DefaultComponentContext(lifecycle)
            di.instance<MultiWindowRootScreenComponentFactory>().create(logPath, mappingPath, context)
        }
        application {
            rootScreenComponent.Render(Modifier)
            LaunchedEffect(lifecycle) {
                lifecycle.resume()
            }
        }
    }
}

fun setExitOnUncaughtException() {
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        handler?.uncaughtException(thread, throwable)
        try {
            MainLogger.e(throwable) { "Uncaught exception in thread \"${thread.name}\"" }
            LoggerManager.shutdown()
        } catch (_: Exception) {
            throwable.printStackTrace()
        }
        exitProcess(1)
    }
}

class DebugStateManager(rootComponentFactory: (ComponentContext) -> ComposeComponent) {

    init {
        GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                delay(10.seconds)
                save()
                destroy()
                delay(2.seconds)
                restore()
            }
        }
    }

    private val composeSaveableStateRegistryFlow: MutableStateFlow<SaveableStateRegistry?> =
        MutableStateFlow(createComposeSaveableStateRegistry())
    private var composeSavedState: Map<String, List<Any?>>? = null

    private var composeContext: ComponentContext? = null
    private var composeComponent: ComposeComponent? = null

    fun createComposeSaveableStateRegistry(): SaveableStateRegistry = SaveableStateRegistry(
        restoredValues = composeSavedState,
        canBeSaved = { true },
    )

    fun save() {
        composeSavedState = composeSaveableStateRegistryFlow.value?.performSave()
    }

    fun destroy() {
        composeSaveableStateRegistryFlow.value = null
    }

    fun restore() {
        composeSaveableStateRegistryFlow.value = createComposeSaveableStateRegistry()
        composeSavedState = null
    }

    @Composable
    fun Intercept(content: @Composable (component: ComposeComponent, decomposeLifecycle: LifecycleRegistry) -> Unit) {
        val registry by composeSaveableStateRegistryFlow.collectAsState()
        if (registry != null) {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                content(composeComponent!!, TODO())
            }
        }
    }
}
