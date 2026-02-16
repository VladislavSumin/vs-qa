package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory
@Stable
internal class FilterHintViewModel : ViewModel() {
    private val showHint = MutableStateFlow(false)
    private val selectedItemKey = MutableStateFlow("tag")

    val state = combine(showHint, selectedItemKey) { showHint, selectedItemKey ->
        if (showHint) {
            FilterHintViewState.Show(
                selectedItemKey = selectedItemKey,
                listOf(
                    FilterHintItem("tag"),
                    FilterHintItem("message"),
                    FilterHintItem("timeAfter"),
                ),
            )
        } else {
            FilterHintViewState.Hidden
        }
    }.stateIn(FilterHintViewState.Hidden)

    /**
     * @return был ли запрос выполнен успешно.
     */
    fun onCloseRequest(): Boolean {
        val oldValue = showHint.value
        showHint.value = false
        return oldValue
    }

    fun onShowRequest() {
        showHint.value = true
    }

    fun onSelectNextItem() {
        val state = (state.value as FilterHintViewState.Show)

        selectedItemKey.update { oldKey ->
            val index = state.items.indexOfFirst { it.key == oldKey }
            state.items[(index + 1) % state.items.size].key
        }
    }

    fun onSelectPrevItem() {
        val state = (state.value as FilterHintViewState.Show)

        selectedItemKey.update { oldKey ->
            val index = state.items.indexOfFirst { it.key == oldKey }
            if (index > 0) {
                state.items[(index - 1)].key
            } else {
                state.items[state.items.size - 1].key
            }
        }
    }
}
