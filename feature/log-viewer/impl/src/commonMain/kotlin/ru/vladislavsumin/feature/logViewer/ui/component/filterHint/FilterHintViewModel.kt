package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logViewer.LogLogger

@GenerateFactory
@Stable
internal class FilterHintViewModel(
    @ByCreate private val currentTokenPrediction: Flow<CurrentTokenPrediction?>,
) : ViewModel(), FilterHintUiInteractor {
    /**
     * Предпочтение к показу подсказки. Этот флаг еще не означает что подсказка будет отображена.
     */
    private val showHint = MutableStateFlow(false)

    private val selectedItemKey = MutableStateFlow("tag")

    override val events: Channel<FilterHintUiInteractor.Event> = Channel()

    val state = combine(
        showHint,
        selectedItemKey,
        currentTokenPrediction,
    ) { showHint, selectedItemKey, currentTokenPrediction ->
        if (showHint && currentTokenPrediction != null) {
            val hints = when (currentTokenPrediction.type) {
                CurrentTokenPrediction.Type.Keyword -> keywordFilterHintItems
                CurrentTokenPrediction.Type.SearchType -> typeFilterHintItems
            }
            val items = hints
                .filter { it.name.startsWith(currentTokenPrediction.startText) }
                .map {
                    FilterHintItem(
                        text = it.name,
                        hint = it.hint,
                        selectedPartLength = currentTokenPrediction.startText.length,
                    )
                }
            if (items.isNotEmpty()) {
                FilterHintViewState.Show(selectedItemKey = selectedItemKey, items = items)
            } else {
                FilterHintViewState.Hidden
            }
        } else {
            FilterHintViewState.Hidden
        }
    }
        .onEach {
            // TODO очередной всратый костыль с onEach. Нужно сделать нормальное решение для всего.
            if (it is FilterHintViewState.Show && it.items.none { it.key == selectedItemKey.value }) {
                selectedItemKey.value = it.items.first().key
            }
        }
        .stateIn(FilterHintViewState.Hidden)

    override fun requestShow() {
        onShowRequest()
    }

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

    fun onAcceptCurrentHint() {
        val state = state.value as FilterHintViewState.Show
        val hint = state.items.first { it.key == state.selectedItemKey }
        LogLogger.d { "onAcceptCurrentHint(), hint: $hint" }
        events.trySend(
            FilterHintUiInteractor.Event.AppendText(
                hint.text.substring(hint.selectedPartLength),
            ),
        )
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
