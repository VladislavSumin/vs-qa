package ru.vladislavsumin.qa.feature.deviceLogDump.domain

import java.nio.file.Path

interface DeviceLogDumpInteractor {
    suspend fun dumpLogs(deviceName: String): Result<Path>
}
