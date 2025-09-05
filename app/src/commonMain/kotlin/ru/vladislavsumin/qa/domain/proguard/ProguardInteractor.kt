package ru.vladislavsumin.qa.domain.proguard

import ru.vladislavsumin.qa.ProguardLogger
import ru.vladislavsumin.qa.core.proguardParser.ProguardParser
import ru.vladislavsumin.qa.core.proguardParser.retrace.ProguardRetracer
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.system.measureTimeMillis

interface ProguardInteractor {
    fun deobfuscateClass(obfuscatedClassName: String): String?
    fun deobfuscateStack(data: String): String
}

class ProguardInteractorImpl(val path: Path) : ProguardInteractor {
    private val proguardClassMapping: Map<String, String>
    private val proguardRetracer: ProguardRetracer

    init {
        ProguardLogger.i { "Start parsing proguard $path" }

        val proguardData = read(path)

        val time = measureTimeMillis {
            proguardClassMapping = ProguardParser.parse(proguardData).getOrThrow()
                .filter { !it.obfuscatedName.startsWith($$$"R8$$REMOVED$$CLASS$$") }
                .associate { it.obfuscatedName to it.originalName }
        }
        ProguardLogger.i { "Proguard parsed at $time ms" }

        proguardRetracer = ProguardRetracer(proguardData)
        proguardRetracer.warmup()
    }

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

    override fun deobfuscateClass(obfuscatedClassName: String): String? = proguardClassMapping[obfuscatedClassName]
    override fun deobfuscateStack(data: String): String {
        return proguardRetracer.retrace(data)
    }
}
