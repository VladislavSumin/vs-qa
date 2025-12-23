package ru.vladislavsumin.feature.logRecent.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
internal abstract class LogRecentDao {
    @Query("SELECT * FROM log_recent order by lastOpenTime DESC")
    abstract fun observeAllSortedByLastOpenTime(): Flow<List<LogRecentEntity>>

    @Query("SELECT * FROM log_recent WHERE path = :path LIMIT 1")
    abstract suspend fun getByPath(path: String): LogRecentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(logRecent: LogRecentEntity)

    @Update
    abstract suspend fun update(logRecent: LogRecentEntity)

    @Query("DELETE FROM log_recent WHERE path = :path")
    abstract suspend fun deleteByPath(path: String)

    @Transaction
    open suspend fun updateLastOpenTime(path: String) {
        val old = getByPath(path)
        if (old != null) {
            update(old.copy(lastOpenTime = Instant.now()))
        } else {
            insert(
                LogRecentEntity(
                    path = path,
                    lastOpenTime = Instant.now(),
                ),
            )
        }
    }
}
