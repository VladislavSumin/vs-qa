package ru.vladislavsumin.feature.logRecent.domain

import kotlinx.coroutines.flow.Flow
import ru.vladislavsumin.feature.logRecent.repository.LogRecentRepository
import java.nio.file.Path

internal interface LogRecentInteractorInternal : LogRecentInteractor {
    /**
     * Возвращает список недавних логов в порядке их последнего открытия (новые сверху)
     */
    fun observeRecents(): Flow<List<LogRecent>>

    suspend fun removeRecent(recentLog: LogRecent)
}

internal class LogRecentInteractorImpl(
    private val repository: LogRecentRepository,
) : LogRecentInteractorInternal {
    override suspend fun addOrUpdateRecent(path: Path) {
        repository.updateLastOpenTime(path)
    }

    override fun observeRecents(): Flow<List<LogRecent>> = repository.observeRecent()
    override suspend fun removeRecent(recentLog: LogRecent) = repository.remove(recentLog)
}
