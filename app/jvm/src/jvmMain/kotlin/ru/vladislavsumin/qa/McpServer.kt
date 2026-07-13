package ru.vladislavsumin.qa

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.kodein.di.DirectDI
import org.kodein.di.instance
import ru.vladislavsumin.feature.logViewer.domain.headless.LogHeadlessProcessor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import kotlin.io.path.Path

private val json = Json {
    ignoreUnknownKeys = true;
    encodeDefaults = true
}

private val tools = listOf(
    McpTool(
        name = "open_log",
        description = """Open a log file for analysis. Supports .log and .zip formats.
            |Must be called before query_log or get_log_info.
            |Returns: path, total_records, status (loaded or error).
        """.trimMargin(),
        inputSchema = McpToolSchema(
            properties = mapOf(
                "path" to McpToolProperty("string", "Path to the log file (.log or .zip)"),
            ),
            required = listOf("path"),
        ),
    ),
    McpTool(
        name = "query_log",
        description = """Query the currently opened log file with a filter expression.
            |Must call open_log first.
            |
            |Filter DSL reference:
            |  Fields: tag, pid, tid, thread, message, level, runNumber, timeAfter, timeBefore
            |  Operators: = (case-insensitive contains), := (exact match)
            |  Logic: & (AND), | (OR), ! or - (NOT), () for grouping
            |  Quotes for values with spaces: message="connection failed"
            |  Bare text (no field=) searches the entire log line
            |
            |Level filter: level=ERROR returns ERROR and FATAL (it's a minimum-level filter).
            |level=WARN returns WARN, ERROR, FATAL. level=INFO returns INFO and above.
            |
            |Time filters: timeAfter and timeBefore require full timestamp format
            |matching the log, e.g. timeAfter="2026-07-01T+03:00 14:00:00.000"
            |
            |Output JSON fields:
            |  total (all log records), filtered (after filter), records[] (paginated)
            |  Each record: order, raw, time, level, pid, tid, tag, message
            |  On error: error.type (file_error or filter_parse_error), error.message
        """.trimMargin(),
        inputSchema = McpToolSchema(
            properties = mapOf(
                "filter" to McpToolProperty(
                    "string",
                    "Filter expression, empty = all records. " +
                        "Examples: level=ERROR, tag=Bluetooth & level=ERROR, message=crash, \"NullPointerException\"",
                ),
                "offset" to McpToolProperty("integer", "Pagination offset, default 0"),
                "limit" to McpToolProperty("integer", "Max records to return, default 100"),
            ),
        ),
    ),
    McpTool(
        name = "get_log_info",
        description = """Get metadata about the currently opened log file.
            |Returns: path, total_records. Must call open_log first.
        """.trimMargin(),
        inputSchema = McpToolSchema(properties = emptyMap()),
    ),
    McpTool(
        name = "close_log",
        description = "Close the currently opened log file and free memory.",
        inputSchema = McpToolSchema(properties = emptyMap()),
    ),
)

@Suppress("CyclomaticComplexMethod", "LoopWithTooManyJumpStatements", "TooGenericExceptionCaught")
fun runMcpServer(di: DirectDI) {
    val processor = di.instance<LogHeadlessProcessor>()
    val state = McpState()

    val reader = BufferedReader(InputStreamReader(System.`in`))
    val writer = System.out

    while (true) {
        val line = reader.readLine() ?: break
        if (line.isBlank()) continue

        val request = try {
            json.decodeFromString<JsonRpcRequest>(line)
        } catch (e: Exception) {
            respond(writer, null, error = JsonRpcError(code = -32700, message = "Parse error: ${e.message}"))
            continue
        }

        try {
            when (request.method) {
                "initialize" -> handleInitialize(writer, request.id)

                "notifications/initialized" -> { /* no response */ }

                "tools/list" -> handleToolsList(writer, request.id)

                "tools/call" -> handleToolsCall(writer, request, processor, state)

                "ping" -> respond(writer, request.id, result = buildJsonObject { put("jsonrpc", "2.0") })

                else -> respond(
                    writer,
                    request.id,
                    error = JsonRpcError(code = -32601, message = "Method not found: ${request.method}"),
                )
            }
        } catch (e: Exception) {
            respond(
                writer,
                request.id,
                error = JsonRpcError(code = -32603, message = "Internal error: ${e.message}"),
            )
        }
    }
}

private fun handleInitialize(writer: java.io.PrintStream, id: Int?) {
    val result = buildJsonObject {
        put("protocolVersion", "2024-11-05")
        put(
            "capabilities",
            buildJsonObject {
                put("tools", buildJsonObject {})
            },
        )
        put(
            "serverInfo",
            buildJsonObject {
            put("name", "vs-qa")
            put("version", BuildConfig.version)
        }
        )
    }
    respond(writer, id, result = result)
}

@OptIn(ExperimentalSerializationApi::class)
private fun handleToolsList(writer: java.io.PrintStream, id: Int?) {
    val toolsArray = buildJsonObject {
        put("tools", kotlinx.serialization.json.JsonArray(tools.map { json.encodeToJsonElement(it) }))
    }
    respond(writer, id, result = toolsArray)
}

@Suppress("CyclomaticComplexMethod", "LongMethod")
@OptIn(ExperimentalSerializationApi::class)
private fun handleToolsCall(
    writer: java.io.PrintStream,
    request: JsonRpcRequest,
    processor: LogHeadlessProcessor,
    state: McpState,
) {
    val params = request.params ?: buildJsonObject { }
    val toolName = params["name"]?.jsonPrimitive?.content ?: ""
    val args = params["arguments"]?.jsonObject ?: buildJsonObject { }

    val resultData = when (toolName) {
        "open_log" -> {
            val logPath = Path(args["path"]?.jsonPrimitive?.content ?: "")
            runBlocking {
                val res = processor.process(logPath, "")
                state.logPath = logPath
                state.totalRecords = res.total
                buildJsonObject {
                    put("path", logPath.toString())
                    put("total_records", res.total)
                    put("status", if (res.error != null) "error" else "loaded")
                }
            }
        }

        "query_log" -> {
            val logPath = state.logPath
                ?: return respond(
                    writer,
                    request.id,
                    error = JsonRpcError(code = -32602, message = "No log file open. Use open_log first."),
                )
            val filter = args["filter"]?.jsonPrimitive?.content ?: ""
            val offset = args["offset"]?.jsonPrimitive?.int ?: 0
            val limit = args["limit"]?.jsonPrimitive?.int ?: 100
            runBlocking {
                val res = processor.process(logPath, filter, offset, limit)
                json.encodeToJsonElement(res).jsonObject
            }
        }

        "get_log_info" -> {
            val logPath = state.logPath
                ?: return respond(
                    writer,
                    request.id,
                    error = JsonRpcError(code = -32602, message = "No log file open."),
                )
            buildJsonObject {
                put("path", logPath.toString())
                put("total_records", state.totalRecords)
            }
        }

        "close_log" -> {
            state.logPath = null
            state.totalRecords = 0
            buildJsonObject { put("status", "closed") }
        }

        else -> return respond(
            writer,
            request.id,
            error = JsonRpcError(code = -32602, message = "Unknown tool: $toolName"),
        )
    }

    val content = buildJsonObject {
        put(
            "content",
            kotlinx.serialization.json.JsonArray(
                listOf(
                    buildJsonObject {
                        put("type", "text")
                        put("text", json.encodeToJsonElement(resultData).toString())
                    },
                ),
            ),
        )
    }
    respond(writer, request.id, result = content)
}

private fun respond(writer: java.io.PrintStream, id: Int?, result: JsonObject? = null, error: JsonRpcError? = null) {
    val response = buildJsonObject {
        put("jsonrpc", "2.0")
        if (id != null) put("id", id) else put("id", JsonNull)
        if (result != null) put("result", result)
        if (error != null) {
            put(
                "error",
                buildJsonObject {
                    put("code", error.code)
                    put("message", error.message)
                },
            )
        }
    }
    writer.println(json.encodeToString(response))
    writer.flush()
}

private class McpState {
    var logPath: Path? = null
    var totalRecords: Int = 0
}
