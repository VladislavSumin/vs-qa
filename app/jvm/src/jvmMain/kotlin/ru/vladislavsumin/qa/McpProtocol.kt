package ru.vladislavsumin.qa

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int? = null,
    val method: String,
    val params: JsonObject? = null,
)

@Serializable
data class JsonRpcError(val code: Int, val message: String)

@Serializable
data class McpTool(val name: String, val description: String, val inputSchema: McpToolSchema)

@Serializable
data class McpToolSchema(
    val type: String = "object",
    val properties: Map<String, McpToolProperty>,
    val required: List<String> = emptyList(),
)

@Serializable
data class McpToolProperty(val type: String, val description: String)
