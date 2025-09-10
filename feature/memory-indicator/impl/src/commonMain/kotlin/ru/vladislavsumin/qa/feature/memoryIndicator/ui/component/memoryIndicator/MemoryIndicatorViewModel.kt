package ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator

import androidx.compose.runtime.Stable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import ru.vladislavsumin.core.decompose.components.ViewModel

@Stable
internal class MemoryIndicatorViewModel : ViewModel() {
    val state: StateFlow<Pair<Long, Long>> = flow {
        val runtime = Runtime.getRuntime()
        while (true) {
            val total = runtime.totalMemory() / MB
            val used = total - runtime.freeMemory() / MB
            emit(used to total)
            delay(CHECK_INTERVAL)
        }
    }.stateIn(initialValue = 1L to 1L)

    fun onClick() {
        @Suppress("ExplicitGarbageCollectionCall")
        Runtime.getRuntime().gc()
    }

    companion object {
        private const val MB = 1024 * 1024
        private const val CHECK_INTERVAL = 500L
    }
}
