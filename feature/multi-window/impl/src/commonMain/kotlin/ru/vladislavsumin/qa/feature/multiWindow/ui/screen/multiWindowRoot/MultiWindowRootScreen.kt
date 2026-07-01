package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.host.childNavigationSlot
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window.WindowScreenParams

@GenerateScreenFactory
internal class MultiWindowRootScreen(context: ComponentContext) : Screen(context) {

//    private val tabs = childNavigationPages(
//        navigationHost = MultiWindowNavigationHost,
//        pageStatus = { index, pages ->
//            if (index == pages.selectedIndex) {
//                ChildNavState.Status.RESUMED
//            } else {
//                ChildNavState.Status.CREATED
//            }
//        },
//        initialPages = { Pages(items = listOf(WindowScreenParams("default")), selectedIndex = 0) },
//    )

    private val tabs = childNavigationSlot(
        navigationHost = MultiWindowNavigationHost,
        initialConfiguration = { WindowScreenParams("default") },
    )

    @Composable
    override fun Render(modifier: Modifier) {
        tabs.value.child?.instance?.Render(modifier)
    }
}
