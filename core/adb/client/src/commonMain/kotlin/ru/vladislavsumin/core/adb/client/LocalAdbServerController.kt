package ru.vladislavsumin.core.adb.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Позволяет управлять состоянием локального ADB сервера.
 */
internal class LocalAdbServerController {
    private val adbExecutable = findAdbExecutable()

    /**
     * Ищет бинарник `adb` и запускает локальный ADB сервер
     * с настройками по умолчанию (порт 5037 и т.п.).
     */
    suspend fun startAdbServer() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(adbExecutable, "start-server")
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val output = process.inputStream.bufferedReader().readText()
            error("Can't start adb server (exitCode=$exitCode). Error: $output")
        }
    }

    private fun findAdbExecutable(): String {
        val env = System.getenv()
        val candidates = mutableListOf<File>()

        fun addFromSdkEnv(varName: String) {
            val root = env[varName] ?: return
            val dir = File(root, "platform-tools")
            candidates += File(dir, adbFileName())
        }

        addFromSdkEnv("ANDROID_HOME")
        addFromSdkEnv("ANDROID_SDK_ROOT")

        env["PATH"]
            ?.split(File.pathSeparator)
            ?.map(::File)
            ?.forEach { dir ->
                candidates += File(dir, adbFileName())
            }

        val found = candidates.firstOrNull { it.exists() && it.canExecute() }
        if (found != null) {
            return found.absolutePath
        }

        // Фоллбек – надеемся, что `adb` доступен в PATH.
        return "adb"
    }

    private fun adbFileName(): String {
        val osName = System.getProperty("os.name").lowercase()
        val isWindows = osName.contains("windows")
        return if (isWindows) "adb.exe" else "adb"
    }
}
