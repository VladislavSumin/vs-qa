package ru.vladislavsumin.feature.logRecent

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.feature.logRecent.db.createLogRecentDatabase
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractor
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractorImpl
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractorInternal
import ru.vladislavsumin.feature.logRecent.repository.LogRecentRepositoryImpl
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactory
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentComponentFactoryImpl
import ru.vladislavsumin.feature.logRecent.ui.component.logRecent.LogRecentViewModelFactory

fun Modules.featureLogRecent() = DI.Module("feature-logRecent") {
    bindSingleton<LogRecentInteractorInternal> {
        val db = createLogRecentDatabase()
        val repository = LogRecentRepositoryImpl(db)
        LogRecentInteractorImpl(repository)
    }
    bindSingleton<LogRecentInteractor> { i<LogRecentInteractorInternal>() }

    bindSingleton<LogRecentComponentFactory> {
        val vmf = LogRecentViewModelFactory(i())
        LogRecentComponentFactoryImpl(vmf)
    }
}
