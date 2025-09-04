package ru.vladislavsumin.qa.domain.proguard

import ru.vladislavsumin.qa.ProguardLogger
import java.nio.file.Path
import kotlin.system.measureTimeMillis

interface ProguardInteractor {
    fun deobfuscateClass(obfuscatedClassName: String): String?
}

class ProguardInteractorImpl(val path: Path) : ProguardInteractor {
    private val proguardClassMapping: Map<String, String>

    init {
        ProguardLogger.i { "Start parsing proguard $path" }
        val time = measureTimeMillis {
            proguardClassMapping = ProguardParser().parse(path).getOrThrow()
                .filter { !it.obfuscatedName.startsWith($$$"R8$$REMOVED$$CLASS$$") }
                .associate { it.obfuscatedName to it.originalName }
        }
        ProguardLogger.i { "Proguard parsed at $time ms" }
    }

    override fun deobfuscateClass(obfuscatedClassName: String): String? = proguardClassMapping[obfuscatedClassName]
}
