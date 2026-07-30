package ru.vladislavsumin.feature.mcp.domain

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i

fun Modules.featureMcp(version: String) = DI.Module("feature-mcp") {
    bindSingleton<McpServer> { McpServerImpl(version, i()) }
}
