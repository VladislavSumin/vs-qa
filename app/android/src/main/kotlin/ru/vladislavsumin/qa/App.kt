package ru.vladislavsumin.qa

import android.app.Application
import android.content.Context
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindProvider
import ru.vladislavsumin.core.di.Modules

class App :
    Application(),
    DIAware {
    private var _di: DI? = null
    override val di: DI
        get() = _di!!

    override fun onCreate() {
        super.onCreate()

        // TODO вынести в отдельный core модуль
//        SentryAndroid.init(this) { options ->
//            options.dsn = "https://ac13621e67953007e14fcfd5642531c4@o512687.ingest.us.sentry.io/4510488819793920"
//        }

        _di = preInit(Modules.android()).di
        MainLogger.i("App#onCreate()")
    }

    private fun Modules.android() = DI.Module("android") {
        bindProvider<Context> { this@App }
    }
}
