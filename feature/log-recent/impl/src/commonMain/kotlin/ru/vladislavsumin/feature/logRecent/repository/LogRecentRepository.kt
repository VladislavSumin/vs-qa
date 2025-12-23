package ru.vladislavsumin.feature.logRecent.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.feature.logRecent.db.LogRecentDatabase
import ru.vladislavsumin.feature.logRecent.db.LogRecentEntity
import ru.vladislavsumin.feature.logRecent.domain.LogRecent
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal interface LogRecentRepository {
    fun observeRecent(): Flow<List<LogRecent>>
    suspend fun remove(logRecent: LogRecent)
    suspend fun updateLastOpenTime(path: Path)
}

internal class LogRecentRepositoryImpl(
    db: LogRecentDatabase,
) : LogRecentRepository {
    private val logRecentDao = db.logRecentDao
    override fun observeRecent(): Flow<List<LogRecent>> =
        logRecentDao.observeAllSortedByLastOpenTime().map { it.toDomain() }

    override suspend fun updateLastOpenTime(path: Path) {
        logRecentDao.updateLastOpenTime(path.absolutePathString())
    }

    override suspend fun remove(logRecent: LogRecent) {
        logRecentDao.deleteByPath(logRecent.path.toString())
    }
}

private fun List<LogRecentEntity>.toDomain(): List<LogRecent> = map { it.toDomain() }

private fun LogRecentEntity.toDomain(): LogRecent = LogRecent(
    path = Path(path),
    lastOpenTime = lastOpenTime,
)
