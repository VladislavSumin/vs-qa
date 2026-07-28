package ru.vladislavsumin.core.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.logger.api.logger

private val LOG = logger("FrameMeasure")
private const val RED_FLASH_DURATION_MS = 300L
private const val NANOS_PER_MILLISECOND = 1_000_000L
private const val GC_THRESHOLD_MULTIPLIER = 3

@Composable
fun FrameMeasureOverlay(
    modifier: Modifier = Modifier,
    thresholdMs: Long = 24,
    logSlowFrames: Boolean = true,
    flashOnSlowFrame: Boolean = true,
    gcThresholdMs: Long = thresholdMs * GC_THRESHOLD_MULTIPLIER,
    content: @Composable () -> Unit,
) {
    val thresholdNanos = thresholdMs * NANOS_PER_MILLISECOND
    val gcThresholdNanos = gcThresholdMs * NANOS_PER_MILLISECOND

    var showRedFlash by remember { mutableStateOf(false) }

    if (logSlowFrames || flashOnSlowFrame) {
        LaunchedEffect(logSlowFrames, flashOnSlowFrame, thresholdMs, gcThresholdMs) {
            var prevFrameNanos = 0L
            var flashResetJob: Job? = null

            while (isActive) {
                val gcCountBefore = totalGcCollections()

                withFrameNanos { frameNanos ->
                    if (prevFrameNanos > 0) {
                        val delta = frameNanos - prevFrameNanos
                        val gcDetected = totalGcCollections() > gcCountBefore
                        val shouldReport = if (gcDetected) {
                            delta > gcThresholdNanos
                        } else {
                            delta > thresholdNanos
                        }
                        if (shouldReport) {
                            if (logSlowFrames) {
                                val msg = buildString {
                                    append("Slow frame: ${delta / NANOS_PER_MILLISECOND}ms ")
                                    if (gcDetected) {
                                        append("(GC detected, threshold: ${gcThresholdMs}ms)")
                                    } else {
                                        append("(threshold: ${thresholdMs}ms)")
                                    }
                                }
                                LOG.w(msg)
                            }
                            if (flashOnSlowFrame) {
                                showRedFlash = true
                                flashResetJob?.cancel()
                                flashResetJob = launch {
                                    delay(RED_FLASH_DURATION_MS)
                                    showRedFlash = false
                                }
                            }
                        }
                    }
                    prevFrameNanos = frameNanos
                }
            }
        }
    }

    Box(modifier = modifier) {
        content()
        if (showRedFlash) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.3f)),
            )
        }
    }
}
