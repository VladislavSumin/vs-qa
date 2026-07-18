package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ChunkedFlowTest {

    @Test
    fun `items within interval are emitted as single batch after interval`() = runTest {
        val source = MutableSharedFlow<Int>()
        val batches = mutableListOf<List<Int>>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.chunked(250.milliseconds).collect { batches.add(it) }
        }

        source.emit(1)
        source.emit(2)
        source.emit(3)
        assertTrue(batches.isEmpty(), "Batch should not be emitted before interval")

        advanceTimeBy(251)
        assertEquals(listOf(listOf(1, 2, 3)), batches)
        job.cancel()
    }

    @Test
    fun `items in different windows are emitted as separate batches`() = runTest {
        val source = MutableSharedFlow<Int>()
        val batches = mutableListOf<List<Int>>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.chunked(250.milliseconds).collect { batches.add(it) }
        }

        source.emit(1)
        advanceTimeBy(251)
        source.emit(2)
        source.emit(3)
        advanceTimeBy(251)

        assertEquals(listOf(listOf(1), listOf(2, 3)), batches)
        job.cancel()
    }

    @Test
    fun `maxSize flushes immediately without advancing time`() = runTest {
        val source = MutableSharedFlow<Int>()
        val batches = mutableListOf<List<Int>>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.chunked(250.milliseconds, maxSize = 2).collect { batches.add(it) }
        }

        source.emit(1)
        source.emit(2)
        runCurrent()
        assertEquals(listOf(listOf(1, 2)), batches)

        source.emit(3)
        assertEquals(1, batches.size, "Third item should wait for interval")
        advanceTimeBy(251)
        assertEquals(listOf(listOf(1, 2), listOf(3)), batches)
        job.cancel()
    }

    @Test
    fun `remainder is flushed on upstream completion`() = runTest {
        val result = flowOf(1, 2, 3).chunked(250.milliseconds).toList()
        assertEquals(listOf(listOf(1, 2, 3)), result)
    }

    @Test
    fun `empty flow emits nothing`() = runTest {
        val result = emptyFlow<Int>().chunked(250.milliseconds).toList()
        assertTrue(result.isEmpty())
    }
}
