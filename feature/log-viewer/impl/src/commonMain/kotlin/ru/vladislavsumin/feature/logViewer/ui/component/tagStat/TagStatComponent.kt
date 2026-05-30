package ru.vladislavsumin.feature.logViewer.ui.component.tagStat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord

internal class TagStatComponent(private val logs: Flow<List<LogRecord>>, context: ComponentContext) :
    Component(context),
    ComposeComponent {
    private val viewModel: TagStatViewModel = viewModel { TagStatViewModel(logs) }

    @Composable
    override fun Render(modifier: Modifier) = TagStatContent(viewModel, modifier)
}
