package ru.vladislavsumin.feature.logViewer.domain.logs

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Собирает элементы потока в пачки.
 *
 * Пачка эмитится когда истекает [interval] с момента поступления первого элемента в пачку,
 * либо раньше, если размер пачки достиг [maxSize]. При завершении исходного потока остаток
 * буфера эмитится сразу. Пустые пачки не эмитятся.
 */
internal fun <T> Flow<T>.chunked(interval: Duration, maxSize: Int = Int.MAX_VALUE): Flow<List<T>> = channelFlow {
    val mutex = Mutex()
    var buffer = ArrayList<T>()
    var flushJob: Job? = null

    suspend fun flush() = mutex.withLock {
        if (buffer.isNotEmpty()) {
            send(buffer)
            buffer = ArrayList()
        }
    }

    this@chunked.collect { item ->
        val flushNow = mutex.withLock {
            buffer.add(item)
            if (buffer.size >= maxSize) {
                flushJob?.cancel()
                flushJob = null
                true
            } else {
                if (flushJob?.isActive != true) {
                    flushJob = launch {
                        delay(interval)
                        flush()
                    }
                }
                false
            }
        }
        if (flushNow) flush()
    }

    flushJob?.cancel()
    flush()
}
