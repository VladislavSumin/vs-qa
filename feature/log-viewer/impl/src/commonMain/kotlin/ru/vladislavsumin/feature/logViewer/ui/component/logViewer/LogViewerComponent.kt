package ru.vladislavsumin.feature.logViewer.ui.component.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.coroutines.utils.mapState
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarComponent
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsComponent
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import java.nio.file.Path

internal class LogViewerComponent(
    logPath: Path,
    mappingPath: Path?,
    bottomBarUiInteractor: BottomBarUiInteractor,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val rootFocusRequester = FocusRequester()
    private val filterFocusRequester = FocusRequester()

    private val filterBarComponent = FilterBarComponent(
        onFocusLost = { rootFocusRequester.requestFocus() },
        focusRequester = filterFocusRequester,
        context = context.childContext("filter-bar"),
    )

    private val viewModel = viewModel {
        LogViewerViewModel(
            logPath = logPath,
            mappingPath = mappingPath,
            bottomBarUiInteractor = bottomBarUiInteractor,
            filterBarUiInteractor = filterBarComponent.filterBarUiInteractor,
        )
    }

    private val logsComponent = LogsComponent(
        logsEvents = viewModel.events,
        state = viewModel.state.mapState { it.logsViewState },
        context = context.childContext("logs"),
    )

    @Composable
    override fun Render(modifier: Modifier) = LogViewerContent(
        viewModel = viewModel,
        rootFocusRequester = rootFocusRequester,
        filterFocusRequester = filterFocusRequester,
        filterBarComponent = filterBarComponent,
        logsComponent = logsComponent,
        modifier = modifier,
    )
}
