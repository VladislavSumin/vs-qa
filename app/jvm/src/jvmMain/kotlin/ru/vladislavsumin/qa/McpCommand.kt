package ru.vladislavsumin.qa

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.kodein.di.DirectDI
import org.kodein.di.instance
import ru.vladislavsumin.feature.logViewer.domain.headless.LogHeadlessProcessor

private val json = Json { prettyPrint = true }

class McpCommand(private val di: DirectDI) : CliktCommand(name = "--mcp") {
    private val log by option("--log").path(mustExist = true, canBeFile = true).required()
    private val filter by option("--filter").default("")
    private val offsetStr by option("--offset").default("0")
    private val limitStr by option("--limit").default("100")

    override fun run() {
        val processor = di.instance<LogHeadlessProcessor>()
        val result = runBlocking {
            processor.process(
                logPath = log,
                filterExpression = filter,
                offset = offsetStr.toInt(),
                limit = limitStr.toInt(),
            )
        }
        println(json.encodeToString(result))
    }
}
