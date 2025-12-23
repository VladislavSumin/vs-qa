package ru.vladislavsumin.feature.logRecent.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "log_recent")
data class LogRecentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val path: String,
    val lastOpenTime: Instant,
)
