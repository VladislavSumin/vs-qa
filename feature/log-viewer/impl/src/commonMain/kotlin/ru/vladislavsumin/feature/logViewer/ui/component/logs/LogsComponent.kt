package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

internal class LogsComponent(
    private val logsEvents: ReceiveChannel<LogsEvents>,
    private val state: StateFlow<LogsViewState>,
    private val onFirstVisibleIndexChange: (Int) -> Unit,
    context: ComponentContext,
) : Component(context), ComposeComponent {

    @Composable
    override fun Render(modifier: Modifier) = LogsContent(onFirstVisibleIndexChange, logsEvents, state, modifier)
}
