package ru.vladislavsumin.feature.logRecent.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.kodein.di.DirectDI
import org.kodein.di.instance
import ru.vladislavsumin.core.fs.FileSystemService
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal actual fun DirectDI.createLogRecentDatabaseBuilder(): RoomDatabase.Builder<LogRecentDatabase> {
    // TODO убрать дублирующийся код
    val context = instance<Context>()
    val fs = instance<FileSystemService>()
    val path = Path(fs.getDatabaseDir().toString()).resolve("recent_logs.db").absolutePathString()
    return Room.databaseBuilder<LogRecentDatabase>(
        context = context,
        name = path,
    ) { LogRecentDatabase_Impl() }
        .setDriver(BundledSQLiteDriver())
}
