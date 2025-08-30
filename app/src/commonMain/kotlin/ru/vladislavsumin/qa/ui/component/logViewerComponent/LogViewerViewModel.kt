package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogsInteractorImpl
import kotlin.io.path.Path

@Stable
internal class LogViewerViewModel : ViewModel() {
    private val filter = MutableStateFlow("")
    private val isFilterUseRegex = MutableStateFlow(false)

    private val logsInteractor = LogsInteractorImpl(Path("../test_log.log"))
    val state = combine(
        filter,
        isFilterUseRegex,
    ) { filter, isFilterUseRegex ->
        // TODO regex use?
        val filteredLogs = logsInteractor.filterLogs(filter)
        LogViewerViewState(
            filter = filter,
            isFilterUseRegex = isFilterUseRegex,
            logs = filteredLogs,
            maxLogNumberDigits = logsInteractor.logs.last().order.toString().length,
        )
    }.stateIn(
        LogViewerViewState(
            filter = "",
            isFilterUseRegex = false,
            logs = emptyList(),
            maxLogNumberDigits = 0,
        )
    )

    fun onFilterChange(newValue: String) {
        filter.value = newValue
    }

    fun onClickFilterUseRegex(newValue: Boolean) {
        isFilterUseRegex.value = newValue
    }
}