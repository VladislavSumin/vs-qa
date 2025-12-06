package ru.vladislavsumin.qa.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.qa.MainLogger
import ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen.RootScreenComponentFactory
import java.nio.file.Path
import java.util.UUID

class MainActivity : ComponentActivity(), DIAware {
    override val di: DI by closestDI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Обрабатываем стартовый deeplink только если это холодный запуск приложения.
        // В противном случае мы уже обработали этот диплинк при первом запуске.
        if (savedInstanceState == null) {
            intent?.let { processIntent(it) }
        }

        val defaultContext = defaultComponentContext()
        val rootComponentFactory = di.direct.instance<RootScreenComponentFactory>()
        val rootComponent = rootComponentFactory.create(null, null, defaultContext)

        setContent {
            rootComponent.Render(Modifier)
        }
    }

    private fun processIntent(intent: Intent) {
        MainLogger.d { "processIntent(): intent = $intent" }
        when (intent.action) {
            Intent.ACTION_SEND -> processActionSendIntent(intent)
            else -> MainLogger.w { "processIntent(): unknown intent" }
        }
    }

    // TODO вот эту все ересь нормально написать.
    private fun processActionSendIntent(intent: Intent) {
        val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)!!
        val path = uriToFile(uri)
        di.direct.instance<Navigation>().open(LogViewerScreenParams(path))
    }

    private fun uriToFile(uri: Uri): Path {
        contentResolver.openInputStream(uri).use { inputStream ->
            val name = getFileNameFromUri(this, uri)!!
            val ext = name.takeLastWhile { it != '.' }
            val cache = cacheDir.resolve("${UUID.randomUUID()}.$ext")
            val bytes = inputStream!!.readAllBytes()
            cache.writeBytes(bytes)
            return cache.toPath()
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) { // Check if the column exists
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }
}
