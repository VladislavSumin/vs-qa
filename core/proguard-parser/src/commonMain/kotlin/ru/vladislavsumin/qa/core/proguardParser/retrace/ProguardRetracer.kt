package ru.vladislavsumin.qa.core.proguardParser.retrace

import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.retrace.ProguardMapProducer
import com.android.tools.r8.retrace.ProguardMappingSupplier
import com.android.tools.r8.retrace.Retrace
import com.android.tools.r8.retrace.RetraceCommand
import ru.vladislavsumin.core.logger.api.logger
import kotlin.system.measureTimeMillis

class ProguardRetracer(mapping: String) {
    private val diagnosticsHandler = object : DiagnosticsHandler {}

    private val proguardMappingSupplier = ProguardMappingSupplier.builder()
        .setProguardMapProducer(ProguardMapProducer.fromString(mapping))
        .setAllowExperimental(false)
        .setLoadAllDefinitions(true)
        .build()

    fun warmup() {
        logger.i { "Warmup mapping" }
        val time = measureTimeMillis {
            proguardMappingSupplier.getMapVersions(diagnosticsHandler)
        }
        logger.i { "Warmup finished at ${time}ms" }
    }

    fun retrace(data: String): String {
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
