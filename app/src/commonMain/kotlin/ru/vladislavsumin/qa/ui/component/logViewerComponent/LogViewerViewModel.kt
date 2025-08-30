package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogsInteractorImpl
import kotlin.io.path.Path

@Stable
internal class LogViewerViewModel : ViewModel() {
    private val filter = MutableStateFlow("")

    private val logsInteractor = LogsInteractorImpl(Path("../test_log.log"))
    val state = filter.mapLatest { filter ->
        val filteredLogs = logsInteractor.filterLogs(filter)
        LogViewerViewState(
            filter = filter,
            logs = filteredLogs,
            maxLogNumberDigits = logsInteractor.logs.last().order.toString().length,
        )
    }.stateIn(
        LogViewerViewState(
            filter = "",
            logs = emptyList(),
            maxLogNumberDigits = 0,
        )
    )

    fun onFilterChange(newValue: String) {
        filter.value = newValue
    }
}