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

    override suspend fun updateMappingPath(path: Path, mappingPath: Path?) {
        repository.updateMapping(path, mappingPath)
    }

    override suspend fun getMappingPath(path: Path): Path? {
        return repository.get(path)?.mappingPath
    }

    override suspend fun updateSearchState(
        path: Path,
        searchRequest: String,
        filterRequest: String,
    ) = repository.updateSearchState(path, searchRequest, filterRequest)

    override suspend fun getSearchState(path: Path): Pair<String, String>? {
        return repository.get(path)?.let { it.searchRequest to it.filterRequest }
    }

    override fun observeRecents(): Flow<List<LogRecent>> = repository.observeRecent()
    override suspend fun removeRecent(recentLog: LogRecent) = repository.remove(recentLog)
}
