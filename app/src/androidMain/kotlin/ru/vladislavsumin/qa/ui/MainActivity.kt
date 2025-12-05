package ru.vladislavsumin.qa.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import kotlinx.coroutines.channels.Channel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactory

class MainActivity : ComponentActivity(), DIAware {
    override val di: DI by closestDI()

    /**
     * Канал в который отправляются все пришедшие диплинки.
     */
    // TODO поддержать диплинки
    private val deeplinkChannel = Channel<String>(capacity = Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Обрабатываем стартовый deeplink только если это холодный запуск приложения.
        // В противном случае мы уже обработали этот диплинк при первом запуске.
        if (savedInstanceState == null) {
            intent?.data?.let { onDeeplink(it.toString()) }
        }

        val defaultContext = defaultComponentContext()
        val rootComponentFactory = di.direct.instance<RootScreenComponentFactory>()
        val rootComponent = rootComponentFactory.create(null, null, defaultContext)

        setContent {
            rootComponent.Render(Modifier)
        }
    }

    private fun onDeeplink(deeplink: String) {
        // открывать диплинки можно так:
        // -W -a android.intent.action.VIEW -d "vs-control://DebugScreenParams" ru.vs.control
        deeplinkChannel.trySend(deeplink).getOrThrow()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { onDeeplink(it.toString()) }
    }
}
