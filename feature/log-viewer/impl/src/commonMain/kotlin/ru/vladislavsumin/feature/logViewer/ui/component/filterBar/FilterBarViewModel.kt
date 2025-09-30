package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.input.TextFieldValue
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier

@GenerateFactory
internal class FilterBarViewModel(
    private val globalHotkeyManager: GlobalHotkeyManager,
) : ViewModel(), FilterBarUiInteractor {
    private val filterRequestParser = FilterRequestParser()
    private val filter = MutableStateFlow(TextFieldValue())
    private val showHelpMenu = MutableStateFlow(false)

    override val filterState: SharedFlow<FilterRequestParser.ParserResult> = filter.map { filter ->
        filterRequestParser.tokenize(filter.text)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val state = combine(
        filter,
        filterState,
        showHelpMenu,
    ) { filter, filterState, showHelpMenu ->
        FilterBarViewState(
            field = filter,
            highlight = filterState.requestHighlight,
            error = filterState.searchRequest.exceptionOrNull()?.let { it.message ?: "No error message provided" },
            showHelpMenu = showHelpMenu,
        )
    }
        .stateIn(initialValue = FilterBarViewState.STUB)

    val events = Channel<FilterBarEvent>()

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            globalHotkeyManager.subscribe(
                KeyModifier.Command + KeyModifier.Shift + Key.F to {
                    events.trySend(FilterBarEvent.Focus)
                    true
                },
            )
        }
    }

    fun onFilterChange(newValue: TextFieldValue) {
        filter.value = newValue
    }

    fun onClickHelpButton() {
        showHelpMenu.update { !it }
    }

    fun onDismissHelpMenu() {
        showHelpMenu.value = false
    }
}
