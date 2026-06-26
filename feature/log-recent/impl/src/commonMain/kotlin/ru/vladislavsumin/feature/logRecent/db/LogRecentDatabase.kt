package ru.vladislavsumin.feature.logRecent.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import org.kodein.di.DirectDI
import org.kodein.di.instance
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import java.time.Instant

internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection
            .prepare("ALTER TABLE log_recent ADD COLUMN customName TEXT DEFAULT NULL")
            .use { it.step() }
    }
}

@Database(
    version = 5,
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

internal fun DirectDI.createLogRecentDatabase(): LogRecentDatabase {
    val dispatchers = instance<VsDispatchers>()
    return createLogRecentDatabaseBuilder()
        .setQueryCoroutineContext(dispatchers.IO)
        .addMigrations(MIGRATION_4_5)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}

internal expect fun DirectDI.createLogRecentDatabaseBuilder(): RoomDatabase.Builder<LogRecentDatabase>
