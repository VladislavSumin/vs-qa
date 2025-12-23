package ru.vladislavsumin.feature.logRecent

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactory
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactoryImpl

fun Modules.featureLogRecent() = DI.Module("feature-logRecent") {
    bindSingleton<LogRecentComponentFactory> { LogRecentComponentFactoryImpl() }
}
