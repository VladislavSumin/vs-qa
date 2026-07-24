package ru.vladislavsumin.feature.logsDashboard.ui.screen.logsDashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

internal class LogsDashboardScreen(context: ComponentContext) :
    Screen(context),
    TabSupport {

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(name = "Dashboard"),
    )

    @Composable
    override fun RenderScreen(modifier: Modifier) = LogsDashboardContent(modifier)
}
