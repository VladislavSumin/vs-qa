package ru.vladislavsumin.qa.ui.component.logViewerComponent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.qa.domain.logs.LogsInteractorImpl
import ru.vladislavsumin.qa.domain.logs.RawLogRecord
import kotlin.io.path.Path

@Stable
internal class LogViewerViewModel : ViewModel() {
    private val logsInteractor = LogsInteractorImpl(Path("../test_log.log"))
    val state = MutableStateFlow<List<RawLogRecord>>(logsInteractor.logs)
}