package ru.vladislavsumin.core.utils

import kotlin.system.measureTimeMillis

inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<Long, T> {
    var result: T
    val time = measureTimeMillis {
        result = block()
    }
    return time to result
}
