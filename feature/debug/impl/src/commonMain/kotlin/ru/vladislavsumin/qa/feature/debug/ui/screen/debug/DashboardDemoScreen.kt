package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

@GenerateScreenFactory
internal class DashboardDemoScreen(
    context: ComponentContext,
) : Screen(context),
    TabSupport {

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(icon = Icons.Default.Dashboard, allowClose = true),
    )

    @Composable
    override fun Render(modifier: Modifier) = DashboardDemoScreenContent(modifier)
}
