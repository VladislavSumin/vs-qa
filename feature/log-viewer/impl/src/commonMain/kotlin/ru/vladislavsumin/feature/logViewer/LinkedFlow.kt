package ru.vladislavsumin.feature.logViewer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.Component

class LinkedFlow<T> : Flow<T> {
    private val link = MutableStateFlow<Flow<T>?>(null)

    override suspend fun collect(collector: FlowCollector<T>) {
        link.filterNotNull().flatMapConcat { it }.collect(collector)
    }

    fun link(flow: Flow<T>) {
        link.update { old ->
            check(old == null) { "Link may be only one" }
            flow
        }
    }
}

fun <T> Flow<T>.link(linkedFlow: LinkedFlow<T>) {
    linkedFlow.link(this)
}