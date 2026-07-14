package ru.vladislavsumin.feature.mcp.domain

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import ru.vladislavsumin.feature.logViewer.domain.headless.LogHeadlessProcessor
import java.nio.file.Path
import kotlin.io.path.Path

private val mcpJson = Json { encodeDefaults = true }

internal class McpServerImpl(private val processor: LogHeadlessProcessor) : McpServer {

    @Suppress("LongMethod")
    override fun start(version: String) = runBlocking {
        System.setProperty("kotlin-logging.logStartupMessage", "false")
        val state = McpState()

        val server = Server(
            serverInfo = Implementation(name = "vs-qa", version = version),
            options = ServerOptions(
                capabilities = ServerCapabilities(tools = ServerCapabilities.Tools()),
            ),
        )

        server.addTool(
            name = "open_log",
            description = """Open a log file for analysis. Supports .log and .zip formats.
                |Must be called before query_log or get_log_info.
                |Returns: path, total_records, status (loaded or error).
            """.trimMargin(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("path") {
                        put("type", "string")
                        put("description", "Path to the log file (.log or .zip)")
                    }
                },
                required = listOf("path"),
            ),
        ) { request ->
            val logPath = Path(request.arguments?.get("path")?.jsonPrimitive?.content ?: "")
            val total = runCatching {
                processor.parseAndCache(logPath)
            }.getOrElse {
                return@addTool CallToolResult(
                    content = listOf(TextContent("{\"error\":\"${it.message}\"}")),
                    isError = true,
                )
            }
            state.logPath = logPath
            state.totalRecords = total
            CallToolResult(
                content = listOf(
                    TextContent(
                        mcpJson.encodeToString(
                            buildJsonObject {
                                put("path", logPath.toString())
                                put("total_records", total)
                                put("status", "loaded")
                            },
                        ),
                    ),
                ),
            )
        }

        server.addTool(
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
                |  Each record: order, time, level, pid, tid, tag, message
                |  On error: error.type (file_error or filter_parse_error), error.message
            """.trimMargin(),
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("filter") {
                        put("type", "string")
                        put(
                            "description",
                            "Filter expression, empty = all records. " +
                                "Examples: level=ERROR, tag=Bluetooth & level=ERROR, message=crash",
                        )
                    }
                    putJsonObject("offset") {
                        put("type", "integer")
                        put("description", "Pagination offset, default 0")
                    }
                    putJsonObject("limit") {
                        put("type", "integer")
                        put("description", "Max records to return, default 100")
                    }
                },
            ),
        ) { request ->
            val filter = request.arguments?.get("filter")?.jsonPrimitive?.content ?: ""
            val offset = request.arguments?.get("offset")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val limit = request.arguments?.get("limit")?.jsonPrimitive?.content?.toIntOrNull() ?: 100
            val res = processor.query(filter, offset, limit)
            CallToolResult(
                content = listOf(TextContent(mcpJson.encodeToString(res))),
                isError = res.error != null,
            )
        }

        server.addTool(
            name = "get_log_info",
            description = """Get metadata about the currently opened log file.
                |Returns: path, total_records. Must call open_log first.
            """.trimMargin(),
            inputSchema = ToolSchema(properties = buildJsonObject {}),
        ) {
            val logPath = state.logPath
            if (logPath == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("{\"error\":\"No log file open. Use open_log first.\"}")),
                    isError = true,
                )
            }
            CallToolResult(
                content = listOf(
                    TextContent(
                        mcpJson.encodeToString(
                            buildJsonObject {
                                put("path", logPath.toString())
                                put("total_records", state.totalRecords)
                            },
                        ),
                    ),
                ),
            )
        }

        server.addTool(
            name = "close_log",
            description = "Close the currently opened log file and free memory.",
            inputSchema = ToolSchema(properties = buildJsonObject {}),
        ) {
            state.logPath = null
            state.totalRecords = 0
            processor.release()
            CallToolResult(
                content = listOf(TextContent(mcpJson.encodeToString(buildJsonObject { put("status", "closed") }))),
            )
        }

        server.createSession(
            transport = StdioServerTransport(
                input = System.`in`.asSource().buffered(),
                output = System.out.asSink().buffered(),
            ),
        )
        try {
            kotlinx.coroutines.delay(Long.MAX_VALUE)
        } catch (_: CancellationException) {
            // Shutdown signal received
        }
    }
}

private class McpState {
    var logPath: Path? = null
    var totalRecords: Int = 0
}
