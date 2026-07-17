package ru.vladislavsumin.qa.feature.deviceLogDump.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.vladislavsumin.core.adb.client.AdbClient
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.qa.feature.deviceLogDump.deviceLogDumpLogger
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.time.measureTime
import kotlin.time.measureTimedValue
import kotlin.io.path.Path as KtPath

internal class DeviceLogDumpInteractorImpl(private val adbClient: AdbClient, private val dispatchers: VsDispatchers) :
    DeviceLogDumpInteractor {

    override suspend fun dumpLogs(deviceName: String): Result<Path> = withContext(dispatchers.IO) {
        runCatching {
            val (componentResult, findMs) = measureTimedValue { findLogDumpReceiver(deviceName) }
            val component = componentResult
                ?: error("No receiver ending with '$RECEIVER_SUFFIX' found on device $deviceName")

            val pkg = component.substringBefore("/")
            val uuid = UUID.randomUUID().toString().take(UUID_LENGTH)
            val remotePath = "/sdcard/Android/data/$pkg/files/log_dumps/$uuid.zip"

            val bcMs = measureTime {
                adbClient.executeShellCommand(
                    deviceName,
                    "am broadcast -n $component -e uuid $uuid",
                ).unwrap()
            }

            val (fileReady, waitMs) = measureTimedValue { waitForFile(deviceName, remotePath) }
            if (!fileReady) {
                error("Dump timed out")
            }

            val localPath = Files.createTempFile("vs-qa-dump-", ".zip").toString()
            val pullMs = measureTime {
                adbClient.pullFile(deviceName, remotePath, localPath).unwrap()
            }

            kotlin.runCatching {
                adbClient.executeShellCommand(deviceName, "rm $remotePath")
            }

            deviceLogDumpLogger.d {
                "Dump complete: find=$findMs bc=$bcMs wait=$waitMs pull=$pullMs " +
                    "total=${findMs + bcMs + waitMs + pullMs}"
            }

            KtPath(localPath)
        }
    }

    private suspend fun findLogDumpReceiver(deviceName: String): String? {
        val output = adbClient.executeShellCommand(
            deviceName,
            "dumpsys package 2>/dev/null | grep -m1 $RECEIVER_SUFFIX",
        ).unwrap()

        val candidates = output.lines()
            .filter { line ->
                line.substringAfterLast("/", "").endsWith(RECEIVER_SUFFIX)
            }
            .toList()

        deviceLogDumpLogger.d {
            "Scanning dumpsys for '*$RECEIVER_SUFFIX' — found ${candidates.size} candidates: $candidates"
        }

        return candidates
            .firstOrNull()
            ?.trim()
            ?.split(" ")
            ?.lastOrNull()
    }

    private suspend fun waitForFile(deviceName: String, path: String): Boolean {
        repeat(MAX_WAIT_ATTEMPTS) {
            delay(WAIT_DELAY_MS)
            val result = adbClient.executeShellCommand(deviceName, "test -f $path && echo ok")
            if (result is AdbClient.AdbResult.Ok && result.data.trim() == "ok") return true
        }
        return false
    }

    companion object {
        private const val RECEIVER_SUFFIX = "LogDumpReceiver"
        private const val UUID_LENGTH = 8
        private const val MAX_WAIT_ATTEMPTS = 10
        private const val WAIT_DELAY_MS = 500L
    }
}
