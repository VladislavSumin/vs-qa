package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.coroutines.utils.mapState
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterBarComponent
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsComponent
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarUiInteractor
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor

@GenerateFactory(LogViewerScreenFactory::class)
internal class LogViewerScreen(
    viewModelFactory: LogViewerViewModelFactory,
    bottomBarUiInteractor: BottomBarUiInteractor,
    notificationsUiInteractor: NotificationsUiInteractor,
    params: LogViewerScreenParams,
    intents: ReceiveChannel<LogViewerScreenIntent>,
    context: ComponentContext,
) : Screen(context) {
    private val rootFocusRequester = FocusRequester()
    private val filterFocusRequester = FocusRequester()

    private val filterBarComponent = FilterBarComponent(
        onFocusLost = { rootFocusRequester.requestFocus() },
        focusRequester = filterFocusRequester,
        context = context.childContext("filter-bar"),
    )

    private val viewModel = viewModel {
        viewModelFactory.create(
            logPath = params.logPath,
            mappingPath = (intents.tryReceive().getOrNull() as? LogViewerScreenIntent.OpenMapping)?.mappingPath,
            bottomBarUiInteractor = bottomBarUiInteractor,
            filterBarUiInteractor = filterBarComponent.filterBarUiInteractor,
            notificationsUiInteractor = notificationsUiInteractor,
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
