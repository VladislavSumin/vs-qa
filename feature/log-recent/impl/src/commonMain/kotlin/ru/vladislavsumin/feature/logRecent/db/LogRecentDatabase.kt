package ru.vladislavsumin.feature.logRecent.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.kodein.di.DirectDI
import java.time.Instant

@Database(
    version = 3,
    exportSchema = false,
    entities = [
        LogRecentEntity::class,
    ],
)
@TypeConverters(LogRecentDatabaseConverters::class)
internal abstract class LogRecentDatabase : RoomDatabase() {
    abstract val logRecentDao: LogRecentDao
}

internal class LogRecentDatabaseConverters {
    @TypeConverter
    fun fromInstant(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long = instant.toEpochMilli()
}

internal expect fun DirectDI.createLogRecentDatabase(): LogRecentDatabase
