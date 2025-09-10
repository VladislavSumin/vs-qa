package ru.vladislavsumin.feature.logViewer.domain.proguard

import ru.vladislavsumin.qa.core.proguardParser.ProguardRetracer
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.readText

interface ProguardInteractor {
    fun deobfuscateClass(obfuscatedClassName: String): String?
    fun deobfuscateStack(data: String): String
}

class ProguardInteractorImpl(path: Path) : ProguardInteractor {
    private val proguardRetracer = ProguardRetracer(read(path))

    private fun read(path: Path): String {
        return if (path.extension == "zip") {
            ZipInputStream(path.inputStream()).use { zip ->
                zip.nextEntry
                zip.bufferedReader().readText()
            }
        } else {
            path.readText()
        }
    }

    override fun deobfuscateClass(obfuscatedClassName: String): String? {
        return proguardRetracer.retraceClassName(obfuscatedClassName)
    }

    override fun deobfuscateStack(data: String): String {
        return proguardRetracer.retraceStacktrace(data)
    }
}
