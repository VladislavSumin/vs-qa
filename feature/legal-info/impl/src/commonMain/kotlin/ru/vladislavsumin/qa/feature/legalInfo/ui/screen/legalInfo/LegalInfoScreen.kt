package ru.vladislavsumin.qa.feature.legalInfo.ui.screen.legalInfo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.legal_info.impl.generated.resources.Res
import ru.vladislavsumin.feature.legal_info.impl.generated.resources.legal_info_tab_name
import ru.vladislavsumin.qa.feature.tabs.ui.component.tabs.TabSupport

@GenerateScreenFactory
internal class LegalInfoScreen(context: ComponentContext) :
    Screen(context),
    TabSupport {

    override val tabState: StateFlow<TabSupport.TabState> = MutableStateFlow(
        TabSupport.TabState(icon = Icons.Default.Gavel, nameRes = Res.string.legal_info_tab_name),
    )

    @Composable
    override fun RenderScreen(modifier: Modifier) = LegalInfoScreenContent(modifier)
}
