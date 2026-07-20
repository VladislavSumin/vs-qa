package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.builtins.serializer
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.ui.debug.uml.NavigationGraphUmlDiagramComponentFactory
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport
import kotlin.math.absoluteValue
import kotlin.random.Random

@GenerateScreenFactory
internal class DebugScreen(
    umlDiagramComponentFactory: NavigationGraphUmlDiagramComponentFactory,
    viewModelFactory: DebugScreenViewModelFactory,
    context: ComponentContext,
) : Screen(context),
    TabSupport {

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(icon = Icons.Default.BugReport, allowClose = false),
    )

    private val viewModel: DebugScreenViewModel = viewModel { viewModelFactory.create() }
    private val umlComponent = umlDiagramComponentFactory.create(context.childContext("uml"))

    val componentRandom = Random.nextInt().absoluteValue % 100

    private val stateKeeperRandom: Int

    init {
        val saved = context.stateKeeper.consume("debug_screen_random", Int.serializer())
        stateKeeperRandom = (saved as? Int) ?: (Random.nextInt().absoluteValue % 100)
        context.stateKeeper.register("debug_screen_random", Int.serializer()) { stateKeeperRandom }
    }

    @Composable
    override fun Render(modifier: Modifier) = DebugScreenContent(
        viewModel = viewModel,
        umlComponent = umlComponent,
        componentRandom = componentRandom,
        stateKeeperRandom = stateKeeperRandom,
        onOpenDashboardDemo = { navigator.open(DashboardDemoScreenParams) },
        modifier = modifier,
    )
}
