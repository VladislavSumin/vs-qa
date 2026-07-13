package ru.vladislavsumin.qa.feature.settings.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.settings.impl.generated.resources.Res
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_tab_name
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

@GenerateScreenFactory
internal class SettingsScreen(viewModelFactory: SettingsScreenViewModelFactory, context: ComponentContext) :
    Screen(context),
    TabSupport {

    private val viewModel: SettingsScreenViewModel = viewModel { viewModelFactory.create() }

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(icon = Icons.Default.Settings, nameRes = Res.string.settings_tab_name),
    )

    @Composable
    override fun Render(modifier: Modifier) = SettingsScreenContent(viewModel, modifier)
}
