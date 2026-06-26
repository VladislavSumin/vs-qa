package ru.vladislavsumin.feature.logRecent.domain

import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface LogRecentInteractor {
    /**
     * Создает новую или обновляет текущую запись о недавнем открытии того или иного лог файла.
     */
    suspend fun addOrUpdateRecent(path: Path)

    suspend fun updateMappingPath(path: Path, mappingPath: Path?)
    suspend fun getMappingPath(path: Path): Path?

    suspend fun updateLogViewerState(
        path: Path,
        searchRequest: String,
        filterRequest: String,
        selectedSearchIndex: Int,
        scrollPosition: Int,
    )

    suspend fun getLogViewerState(path: Path): LogViewerState?

    fun observeCustomName(path: Path): Flow<String?>

    data class LogViewerState(
        val searchRequest: String,
        val filterRequest: String,
        val selectedSearchIndex: Int,
        val scrollPosition: Int,
    )
}
