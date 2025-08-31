package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogsInteractorImpl
import kotlin.io.path.Path

@Stable
internal class LogViewerViewModel : ViewModel() {
    private val filter = MutableStateFlow("")
    private val search = MutableStateFlow("")
    private val isFilterUseRegex = MutableStateFlow(false)
    private val isSearchUseRegex = MutableStateFlow(false)

    private val logsInteractor = LogsInteractorImpl(Path("../test_log.log"))
    val state = combine(
        filter,
        search,
        isFilterUseRegex,
        isSearchUseRegex
    ) { filter, search, isFilterUseRegex, isSearchUseRegex ->
        // TODO regex use?
        val filteredLogs = logsInteractor.filterAndSearchLogs(filter, search)
        val searchResults = if (search.isEmpty()) 0 else {
            filteredLogs.count { it.searchHighlight != null }
        }

        LogViewerViewState(
            filter = filter,
            search = search,
            isFilterUseRegex = isFilterUseRegex,
            isSearchUseRegex = isSearchUseRegex,
            searchResults = searchResults,
            logs = filteredLogs,
            maxLogNumberDigits = logsInteractor.logs.last().order.toString().length,
        )
    }.stateIn(
        LogViewerViewState(
            filter = "",
            search = "",
            isFilterUseRegex = false,
            isSearchUseRegex = false,
            searchResults = 0,
            logs = emptyList(),
            maxLogNumberDigits = 0,
        )
    )

    fun onVisibleItemsChanged(firstIndex: Int, lastIndex: Int) {
        println("QWQW fi=$firstIndex, li=$lastIndex")
    }

    fun onFilterChange(newValue: String) {
        filter.value = newValue
    }

    fun onSearchChange(newValue: String) {
        search.value = newValue
    }

    fun onClickFilterUseRegex(newValue: Boolean) {
        isFilterUseRegex.value = newValue
    }

    fun onClickSearchUseRegex(newValue: Boolean) {
        isFilterUseRegex.value = newValue
    }
}