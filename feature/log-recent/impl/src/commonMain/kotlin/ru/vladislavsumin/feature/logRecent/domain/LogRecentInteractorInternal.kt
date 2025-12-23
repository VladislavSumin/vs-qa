package ru.vladislavsumin.feature.logRecent.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Path
import java.time.Instant

internal interface LogRecentInteractorInternal : LogRecentInteractor {
    /**
     * Возвращает список недавних логов в порядке их последнего открытия (новые сверху)
     */
    fun observeRecents(): Flow<List<LogRecent>>
}

internal class LogRecentInteractorImpl : LogRecentInteractorInternal {
    private val recents = MutableStateFlow<List<LogRecent>>(emptyList())

    override suspend fun addOrUpdateRecent(path: Path) {
        val absolutePath = path.toAbsolutePath()
        recents.update { old ->
            val new = old.toMutableList()

            val index = new.indexOfFirst { absolutePath == it.path }
            if (index != -1) {
                new[index] = new[index].copy(lastOpenTime = Instant.now())
            } else {
                new.add(
                    LogRecent(
                        path = absolutePath,
                        lastOpenTime = Instant.now(),
                    ),
                )
            }

            new.sortBy { it.lastOpenTime }
            new
        }
    }

    override fun observeRecents(): Flow<List<LogRecent>> = recents
}
