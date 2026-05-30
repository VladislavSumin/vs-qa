package ru.vladislavsumin.qa

import android.app.Application
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindProvider
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.Hotkey

class App :
    Application(),
    DIAware {
    private var _di: DI? = null
    override val di: DI
        get() = _di!!

    override fun onCreate() {
        super.onCreate()
        val ghm = object : GlobalHotkeyManager {
            override suspend fun subscribe(vararg hotkeys: Pair<Hotkey, () -> Boolean>): Nothing {
                suspendCancellableCoroutine<Nothing> {
                    // TODO notimplemented
                }
            }
        }

        // TODO вынести в отдельный core модуль
//        SentryAndroid.init(this) { options ->
//            options.dsn = "https://ac13621e67953007e14fcfd5642531c4@o512687.ingest.us.sentry.io/4510488819793920"
//        }

        _di = preInit(ghm, Modules.android()).di
        MainLogger.i("App#onCreate()")
    }

    private fun Modules.android() = DI.Module("android") {
        bindProvider<Context> { this@App }
    }
}
