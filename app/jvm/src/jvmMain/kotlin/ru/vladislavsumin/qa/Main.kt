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
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
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
        di.instance<McpServer>().start()
        return
    }

    val di = preInit()

    // Хоть как то красит верхнюю системную часть окна.
    System.setProperty("apple.awt.application.appearance", "system")

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

class DebugStateManager(private val rootComponentFactory: (ComponentContext) -> ComposeComponent) {

    enum class Mode { RETAIN_INSTANCE_KEEPER, DESTROY_INSTANCE_KEEPER }

    private class Generation(
        val lifecycle: LifecycleRegistry,
        val stateKeeper: StateKeeperDispatcher,
        val instanceKeeper: InstanceKeeperDispatcher,
        val component: ComposeComponent,
        val composeRegistry: SaveableStateRegistry,
    )

    private val generationFlow = MutableStateFlow<Generation?>(null)

    private var composeSavedState: Map<String, List<Any?>>? = null
    private var decomposeSavedState: SerializableContainer? = null
    private var retainedInstanceKeeper: InstanceKeeperDispatcher? = null

    init {
        generationFlow.value = createGeneration()
        GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                delay(6.seconds)
                // Режим пока захардкожен, позже сделать настраиваемым.
                saveAndDestroy(Mode.DESTROY_INSTANCE_KEEPER)
                delay(1.seconds)
                restore()
            }
        }
    }

    private fun createGeneration(): Generation {
        val lifecycle = LifecycleRegistry()
        val stateKeeper = StateKeeperDispatcher(decomposeSavedState)
        decomposeSavedState = null
        val instanceKeeper = retainedInstanceKeeper ?: InstanceKeeperDispatcher()
        retainedInstanceKeeper = null
        val component = runOnUiThread {
            val context = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                instanceKeeper = instanceKeeper,
            )
            rootComponentFactory(context)
        }
        val composeRegistry = SaveableStateRegistry(
            restoredValues = composeSavedState,
            canBeSaved = { true },
        )
        composeSavedState = null
        return Generation(lifecycle, stateKeeper, instanceKeeper, component, composeRegistry)
    }

    private suspend fun saveAndDestroy(mode: Mode) {
        val generation = generationFlow.value ?: return
        composeSavedState = generation.composeRegistry.performSave()
        decomposeSavedState = generation.stateKeeper.save()
        generationFlow.value = null
        // Даем композиции задиспоузиться до уничтожения компонентов.
        delay(0.5.seconds)
        generation.lifecycle.destroy()
        when (mode) {
            Mode.RETAIN_INSTANCE_KEEPER -> retainedInstanceKeeper = generation.instanceKeeper
            Mode.DESTROY_INSTANCE_KEEPER -> generation.instanceKeeper.destroy()
        }
    }

    private fun restore() {
        generationFlow.value = createGeneration()
    }

    @Composable
    fun Intercept(content: @Composable (component: ComposeComponent, decomposeLifecycle: LifecycleRegistry) -> Unit) {
        val generation by generationFlow.collectAsState()
        generation?.let {
            CompositionLocalProvider(LocalSaveableStateRegistry provides it.composeRegistry) {
                content(it.component, it.lifecycle)
            }
        }
    }
}
