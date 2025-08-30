package ru.vladislavsumin.qa.ui.component.memoryIndicatorComponent

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
            val total = runtime.totalMemory() / 1024 / 1024
            val used = total - runtime.freeMemory() / 1024 / 1024
            emit(used to total)
            delay(500L)
        }
    }.stateIn(initialValue = 1L to 1L)

    fun onClick() {
        Runtime.getRuntime().gc()
    }
}