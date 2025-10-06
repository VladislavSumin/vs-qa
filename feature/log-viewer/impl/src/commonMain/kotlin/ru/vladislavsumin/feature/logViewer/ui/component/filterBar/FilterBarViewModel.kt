package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okio.Path.Companion.toOkioPath
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import kotlin.io.path.Path
import kotlin.io.path.absolute

@GenerateFactory
internal class FilterBarViewModel(
    private val globalHotkeyManager: GlobalHotkeyManager,
) : ViewModel(), FilterBarUiInteractor {
    private val filter = MutableStateFlow(TextFieldValue())

    private val savedFiltersPreferenceKey = stringPreferencesKey("saved_filters")

    // TODO перенести на правильный слой архитектуры
    private val prefs = PreferenceDataStoreFactory.createWithPath(
        produceFile = { Path("~/.vs-qa/data/saved_filters.preferences_pb").absolute().toOkioPath() },
    )

    private val showHelpMenu = MutableStateFlow(false)

    private val showSavedFilters = MutableStateFlow(false)
    private val saveNewFilterName = MutableStateFlow("")
    private val saveNewFilterContent = MutableStateFlow("")

    private val savedFilters = prefs.data.map { preferences ->
        preferences[savedFiltersPreferenceKey]?.let {
            Json.decodeFromString<List<FilterBarViewState.SavedFiltersState.SavedFilter>>(it)
        } ?: emptyList()
    }.stateIn(emptyList())

    private val filterRequestParser = FilterRequestParser(savedFilters)

    override val filterState: SharedFlow<FilterRequestParser.ParserResult> = filter.map { filter ->
        filterRequestParser.tokenize(filter.text)
    }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val savedFiltersState = combine(
        showSavedFilters,
        saveNewFilterName,
        saveNewFilterContent,
        savedFilters,
    ) { showSavedFilters, saveNewFilterName, saveNewFilterContent, savedFilters ->
        FilterBarViewState.SavedFiltersState(
            showSavedFilters = showSavedFilters,
            saveNewFilterName = saveNewFilterName,
            saveNewFilterContent = saveNewFilterContent,
            savedFilters = savedFilters,
        )
    }

    val state = combine(
        filter,
        filterState,
        showHelpMenu,
        savedFiltersState,
    ) { filter, filterState, showHelpMenu, savedFiltersState ->
        FilterBarViewState(
            field = filter,
            highlight = filterState.requestHighlight,
            error = filterState.searchRequest.exceptionOrNull()?.let { it.message ?: "No error message provided" },
            showHelpMenu = showHelpMenu,
            savedFiltersState = savedFiltersState,
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

    fun onClickSavedFilters() {
        showSavedFilters.update { !it }
    }

    fun onSavedFilterNameChanged(name: String) {
        saveNewFilterName.value = name
    }

    fun onSavedFilterContentChanged(content: String) {
        saveNewFilterContent.value = content
    }

    fun onClickSaveNewFilter() = launch {
        val newData = savedFilters.value + FilterBarViewState.SavedFiltersState.SavedFilter(
            name = saveNewFilterName.value,
            content = saveNewFilterContent.value,
        )

        if (newData.map { it.name }.toSet().size != newData.size) {
            // TODO сделать нормальную проверку и обработку.
            FilterLogger.w { "Failed to saved more than one filter with same name" }
            return@launch
        }

        prefs.edit { preferences ->
            preferences[savedFiltersPreferenceKey] = Json.encodeToString(newData)
        }
        saveNewFilterName.value = ""
        saveNewFilterContent.value = ""
    }

    fun onDeleteSavedFilter(filter: FilterBarViewState.SavedFiltersState.SavedFilter) = launch {
        val newData = savedFilters.value.toMutableList()
        newData.remove(filter)
        prefs.edit { preferences ->
            preferences[savedFiltersPreferenceKey] = Json.encodeToString(newData)
        }
    }

    fun onClickHelpButton() {
        showHelpMenu.update { !it }
    }

    fun onDismissHelpMenu() {
        showHelpMenu.value = false
    }

    fun highlightSavedFilter(
        filter: FilterBarViewState.SavedFiltersState.SavedFilter,
    ): FilterRequestParser.RequestHighlight {
        return filterRequestParser.justHighlight(filter.content)
    }
}
