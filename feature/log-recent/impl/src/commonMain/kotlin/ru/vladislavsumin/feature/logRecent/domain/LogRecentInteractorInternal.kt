package ru.vladislavsumin.feature.logRecent.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.feature.logRecent.repository.LogRecentRepository
import java.nio.file.Path

internal interface LogRecentInteractorInternal : LogRecentInteractor {
    /**
     * Возвращает список недавних логов в порядке их последнего открытия (новые сверху)
     */
    fun observeRecents(): Flow<List<LogRecent>>

    suspend fun removeRecent(recentLog: LogRecent)

    suspend fun updateCustomName(id: Long, customName: String?)
}

internal class LogRecentInteractorImpl(private val repository: LogRecentRepository) : LogRecentInteractorInternal {
    override suspend fun addOrUpdateRecent(path: Path) {
        repository.updateLastOpenTime(path)
    }

    override suspend fun updateMappingPath(path: Path, mappingPath: Path?) {
        repository.updateMapping(path, mappingPath)
    }

    override suspend fun getMappingPath(path: Path): Path? = repository.get(path)?.mappingPath

    override suspend fun updateLogViewerState(
        path: Path,
        searchRequest: String,
        filterRequest: String,
        selectedSearchIndex: Int,
        scrollPosition: Int,
    ) = repository.updateLogViewerState(path, searchRequest, filterRequest, selectedSearchIndex, scrollPosition)

    override suspend fun getLogViewerState(path: Path): LogRecentInteractor.LogViewerState? =
        repository.get(path)?.let {
            LogRecentInteractor.LogViewerState(
                searchRequest = it.searchRequest,
                filterRequest = it.filterRequest,
                selectedSearchIndex = it.selectedSearchIndex,
                scrollPosition = it.scrollPosition,
            )
        }

    override fun observeRecents(): Flow<List<LogRecent>> = repository.observeRecent()
    override suspend fun removeRecent(recentLog: LogRecent) = repository.remove(recentLog)
    override suspend fun updateCustomName(id: Long, customName: String?) = repository.updateCustomName(id, customName)
    override fun observeCustomName(path: Path): Flow<String?> = observeRecents()
        .map { recents -> recents.find { it.path == path }?.customName }
        .distinctUntilChanged()
}
