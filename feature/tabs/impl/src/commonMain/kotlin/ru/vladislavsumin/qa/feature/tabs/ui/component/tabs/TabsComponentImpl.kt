package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.components.utils.asStateFlow
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor

@GenerateFactory(TabsComponentFactory::class)
internal class TabsComponentImpl(
    private val windowTitleInteractor: WindowTitleInteractor,
    private val pages: Value<ChildPages<ConfigurationHolder, Screen>>,
    private val onTabClick: (IntentScreenParams<*>) -> Unit,
    private val onTabClickClose: (IntentScreenParams<*>) -> Unit,
    context: ComponentContext,
) : Component(context),
    TabsComponent {

    init {
        scope.launch {
            pages.asStateFlow().collectLatest { pages ->
                val item = pages.items.getOrNull(pages.selectedIndex)
                val tab = item?.instance as? TabSupport ?: return@collectLatest
                tab.tabState.collect {
                    windowTitleInteractor.setWindowTitleExtension(it.windowName)
                }
            }
        }
    }

    @Composable
    override fun Render(modifier: Modifier) = TabsContent(pages, onTabClick, onTabClickClose, modifier)
}
