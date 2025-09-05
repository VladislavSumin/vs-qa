package ru.vladislavsumin.qa.domain.proguard

import ru.vladislavsumin.qa.ProguardLogger
import ru.vladislavsumin.qa.core.proguard.ProguardClass
import ru.vladislavsumin.qa.core.proguard.ProguardParser
import ru.vlasidlavsumin.core.stacktraceParser.StackTrace
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.system.measureTimeMillis

interface ProguardInteractor {
    fun deobfuscateClass(obfuscatedClassName: String): String?
    fun deobfuscateMethod(className: String, obfuscatedMethodName: String): String?
    fun deobfuscateStack(stacktrace: StackTrace): StackTrace
}

class ProguardInteractorImpl(val path: Path) : ProguardInteractor {
    private val proguardClassMapping: Map<String, String>
    private val proguardClasses: Map<String, ProguardClass>

    init {
        ProguardLogger.i { "Start parsing proguard $path" }
        val time = measureTimeMillis {
            val classes = parse(path).getOrThrow()
                .filter { !it.obfuscatedName.startsWith($$$"R8$$REMOVED$$CLASS$$") }

            proguardClassMapping = classes.associate { it.obfuscatedName to it.originalName }
            proguardClasses = classes.associateBy { it.originalName }
        }
        ProguardLogger.i { "Proguard parsed at $time ms" }
    }

    private fun parse(path: Path): Result<List<ProguardClass>> {
        return if (path.extension == "zip") {
            ZipInputStream(path.inputStream()).use { zip ->
                zip.nextEntry
                val text = zip.bufferedReader().readText()
                ProguardParser.parse(text)
            }
        } else {
            ProguardParser.parse(path.readText())
        }
    }

    override fun deobfuscateClass(obfuscatedClassName: String): String? = proguardClassMapping[obfuscatedClassName]
    override fun deobfuscateMethod(className: String, obfuscatedMethodName: String): String? =
        proguardClasses[className]?.methods?.firstOrNull { it.obfuscatedName == obfuscatedMethodName }?.originalName

    override fun deobfuscateStack(stacktrace: StackTrace): StackTrace {
        return stacktrace.copy(
            clazz = deobfuscateClass(stacktrace.clazz) ?: stacktrace.clazz,
            elements = stacktrace.elements.map { element ->
                val clazz = deobfuscateClass(element.clazz)
                val method = clazz?.let { deobfuscateMethod(it, element.method) }
                element.copy(
                    clazz = clazz ?: element.clazz,
                    method = method ?: element.method,
                )
            },
        )
    }
}
