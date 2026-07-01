package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.ui.debug.uml.NavigationGraphUmlDiagramComponentFactory
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

@GenerateScreenFactory
internal class DebugScreen(
    umlDiagramComponentFactory: NavigationGraphUmlDiagramComponentFactory,
    context: ComponentContext,
) : Screen(context),
    TabSupport {

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(icon = Icons.Default.BugReport, allowClose = false),
    )

    private val umlComponent = umlDiagramComponentFactory.create(context.childContext("uml"))

    @Composable
    override fun Render(modifier: Modifier) = umlComponent.Render(modifier.fillMaxSize())
}
