package ru.vladislavsumin.qa.core.proguardParser

import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.references.Reference
import com.android.tools.r8.retrace.ProguardMapProducer
import com.android.tools.r8.retrace.ProguardMappingSupplier
import com.android.tools.r8.retrace.Retrace
import com.android.tools.r8.retrace.RetraceCommand
import ru.vladislavsumin.core.logger.api.logger
import kotlin.system.measureTimeMillis

/**
 * Утилита для восстановления стеков вызовов после обфускации.
 *
 * @param mapping строка содержащая в себе полный mapping.txt
 */
class ProguardRetracer(mapping: String) {
    private val diagnosticsHandler = object : DiagnosticsHandler {}

    private val proguardMappingSupplier by lazy {
        val supplier = ProguardMappingSupplier.builder()
            .setProguardMapProducer(ProguardMapProducer.fromString(mapping))
            .setAllowExperimental(false)
            .setLoadAllDefinitions(true)
            .build()

        // Прогреваем. Так как первое чтение не является потокобезопасным.
        logger.i { "Warmup mapping" }
        val time = measureTimeMillis {
            supplier.createRetracer(diagnosticsHandler)
        }
        logger.i { "Warmup finished at ${time}ms" }

        supplier
    }

    /**
     * Ищет [obfuscatedClassName] в mapping и преобразует его в исходное имя класса,
     * если имя не найдено возвращает null.
     */
    fun retraceClassName(obfuscatedClassName: String): String? {
        val retracer = proguardMappingSupplier.createRetracer(diagnosticsHandler)
        val classReference = Reference.classFromTypeName(obfuscatedClassName)
        return retracer.retraceClass(classReference).className
    }

    /**
     * Ищет стеки вызовов в переданной [data] и возвращает исправленную строку с деобфусцированными стеками.
     * Части строки которые не являются стеками, остаются без изменений. Производительность тут так себе, так
     * что пихать все строки без разбору не стоит.
     */
    fun retraceStacktrace(data: String): String {
        var result: String? = null
        val time = measureTimeMillis {
            val builder = RetraceCommand.builder(diagnosticsHandler)
                .setStackTrace(data.lines())
                .setMappingSupplier(proguardMappingSupplier)
                .setRetracedStackTraceConsumer {
                    result = it.joinToString(separator = "\n")
                }
                .build()
            Retrace.run(builder)
        }
        logger.d { "Stacktrace retraced at $time ms" }
        return result!!
    }

    companion object {
        val logger = logger("proguard-retracer")
    }
}
