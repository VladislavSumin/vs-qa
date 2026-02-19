package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.coroutines.utils.mapState
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.LinkedFlow
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
import ru.vladislavsumin.feature.logViewer.ui.component.dragAndDropOverlay.DragAndDropOverlayComponent
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsComponent
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor

@GenerateFactory(LogViewerScreenFactory::class)
internal class LogViewerScreen(
    viewModelFactory: LogViewerViewModelFactory,
    filterBarComponentFactory: FilterBarComponentFactory,
    bottomBarUiInteractor: BottomBarUiInteractor,
    notificationsUiInteractor: NotificationsUiInteractor,
    params: LogViewerScreenParams,
    intents: ReceiveChannel<LogViewerScreenIntent>,
    context: ComponentContext,
) : Screen(context) {
    private val searchFocusRequester = FocusRequester()

    // TODO подумать нормально ли делать так?
    private val currentTagsLink = LinkedFlow<Set<String>>()
    private val currentRunsLink = LinkedFlow<List<RunIdInfo>>()

    private val filterBarComponent = filterBarComponentFactory.create(
        currentTags = currentTagsLink,
        currentRuns = currentRunsLink,
        context = context.childContext("filter-bar"),
    )

    private val viewModel: LogViewerViewModel = viewModel {
        viewModelFactory.create(
            logPath = params.logPath,
            mappingPath = (intents.tryReceive().getOrNull() as? LogViewerScreenIntent.OpenMapping)?.mappingPath,
            currentTags = currentTagsLink,
            currentRuns = currentRunsLink,
            bottomBarUiInteractor = bottomBarUiInteractor,
            filterBarUiInteractor = filterBarComponent.filterBarUiInteractor,
            notificationsUiInteractor = notificationsUiInteractor,
        )
    }

    private val logsComponent = LogsComponent(
        logsEvents = viewModel.logsEvents,
        state = viewModel.state.mapState { it.logsViewState },
        onFirstVisibleIndexChange = viewModel::onFirstVisibleIndexUpdate,
        context = context.childContext("logs"),
    )

    private val dragAndDropOverlayComponent = DragAndDropOverlayComponent(
        onMappingPathSelected = viewModel::onDragAndDropMappingFile,
        onLogPathSelected = viewModel::onDragAndDropLogsFile,
        context = context.childContext("drag-and-drop"),
    )

    init {
        launch {
            viewModel.events.receiveAsFlow().collect { event ->
                when (event) {
                    LogViewerEvent.FocusSearch -> searchFocusRequester.requestFocus()
                }
            }
        }
    }

    @Composable
    override fun Render(modifier: Modifier) = LogViewerContent(
        viewModel = viewModel,
        searchFocusRequester = searchFocusRequester,
        filterBarComponent = filterBarComponent,
        dragAndDropOverlayComponent = dragAndDropOverlayComponent,
        logsComponent = logsComponent,
        modifier = modifier,
    )
}
