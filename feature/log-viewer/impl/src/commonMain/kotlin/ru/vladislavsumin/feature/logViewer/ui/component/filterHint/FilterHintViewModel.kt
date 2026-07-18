package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.coroutines.utils.combine
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logViewer.LogLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository

@GenerateFactory
@Stable
internal class FilterHintViewModel(
    dispatchers: VsDispatchers,
    @ByCreate currentTokenPrediction: Flow<CurrentTokenPrediction?>,
    @ByCreate currentTags: Flow<Set<String>>,
    @ByCreate currentPackages: Flow<Set<String>>,
    @ByCreate currentRuns: Flow<List<RunIdInfo>>,
    @ByCreate savedFilters: Flow<List<SavedFiltersRepository.SavedFilter>>,
) : ViewModel(),
    FilterHintUiInteractor {
    /**
     * Предпочтение к показу подсказки. Этот флаг еще не означает что подсказка будет отображена.
     */
    private val showHint = MutableStateFlow(false)

    private val selectedItemKey = MutableStateFlow("")

    override val events: Channel<FilterHintUiInteractor.Event> = Channel()

    val state = combine(
        showHint,
        selectedItemKey,
        currentTokenPrediction,
        currentTags.map { currentTags ->
            currentTags.map { KeywordFilterHint(it) }
        },
        currentPackages.map { currentPackages ->
            currentPackages.map { KeywordFilterHint(it) }
        },
        currentRuns.map { currentRuns ->
            currentRuns.mapIndexed { index, info ->
                KeywordFilterHint(
                    name = (index + 1).toString(),
                    hint = info.meta.values.joinToString(),
                )
            }
        },
        savedFilters.map { savedFilters ->
            savedFilters.map { KeywordFilterHint(name = it.name, hint = it.content) }
        },
    ) { showHint, selectedItemKey, currentTokenPrediction, currentTags, currentPackages, currentRuns, savedFilters ->
        if (showHint && currentTokenPrediction != null) {
            val hints = when (currentTokenPrediction.type) {
                CurrentTokenPrediction.Type.Keyword -> keywordFilterHintItems + savedFilters

                CurrentTokenPrediction.Type.SearchType -> typeFilterHintItems

                CurrentTokenPrediction.Type.LogLevel -> logLevelFilterHintItems

                CurrentTokenPrediction.Type.Tag -> currentTags

                CurrentTokenPrediction.Type.Package -> currentPackages

                CurrentTokenPrediction.Type.RunNumber -> {
                    if (currentTokenPrediction.startText.startsWith("-")) {
                        currentRuns.reversed().mapIndexed { index, hint ->
                            hint.copy(name = (-(index + 1)).toString())
                        }
                    } else {
                        currentRuns
                    }
                }
            }
            val items = FilterHintSearcher.search(hints, currentTokenPrediction.startText)
            if (items.isNotEmpty() && (items.size > 1 || items.first().text != currentTokenPrediction.startText)) {
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
            if (it is FilterHintViewState.Hidden) {
                selectedItemKey.value = ""
            }
            if (it is FilterHintViewState.Show && it.items.none { it.key == selectedItemKey.value }) {
                selectedItemKey.value = it.items.first().key
            }
        }
        .flowOn(dispatchers.Default)
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

    fun onAcceptCurrentHint(): Boolean {
        val state = state.value as? FilterHintViewState.Show ?: return false
        val hint = state.items.first { it.key == state.selectedItemKey }
        onAcceptHint(hint)
        return true
    }

    fun onAcceptHint(hint: FilterHintItem) {
        LogLogger.d { "onAcceptCurrentHint(), hint: $hint" }
        events.trySend(
            FilterHintUiInteractor.Event.ReplaceText(
                removeLen = hint.searchLength,
                text = hint.text,
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
