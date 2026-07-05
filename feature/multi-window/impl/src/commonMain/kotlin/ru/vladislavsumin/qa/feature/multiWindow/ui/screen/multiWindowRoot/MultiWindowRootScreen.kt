package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.Pages
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenParams

@GenerateScreenFactory
internal class MultiWindowRootScreen(context: ComponentContext) : Screen(context) {

    private val windowsResult = childNavigationPages(
        navigationHost = MultiWindowNavigationHost,
        pageStatus = { index, pages ->
            if (index == pages.selectedIndex) {
                ChildNavState.Status.RESUMED
            } else {
                ChildNavState.Status.CREATED
            }
        },
        initialPages = { Pages(items = listOf(WindowScreenParams("default")), selectedIndex = 0) },
    )
    private val windows = windowsResult.pages

    @Composable
    override fun Render(modifier: Modifier) = MultiWindowRootContent(windows, modifier)
}
