package ru.vladislavsumin.feature.logRecent.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.kodein.di.DirectDI
import org.kodein.di.instance
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.fs.FileSystemService
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal actual fun DirectDI.createLogRecentDatabase(): LogRecentDatabase {
    // TODO убрать дублирующийся код
    val context = instance<Context>()
    val fs = instance<FileSystemService>()
    val dispatchers = instance<VsDispatchers>()
    val path = Path(fs.getDatabaseDir().toString()).resolve("recent_logs.db").absolutePathString()
    return Room.databaseBuilder<LogRecentDatabase>(
        context = context,
        name = path,
    ) { LogRecentDatabase_Impl() }
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatchers.IO)
        .build()
}
