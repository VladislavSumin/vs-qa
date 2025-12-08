package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.factoryGenerator.GenerateScreenFactory
import ru.vladislavsumin.core.navigation.screen.Screen

@GenerateScreenFactory
internal class HomeScreen(
    viewModelFactory: HomeScreenViewModelFactory,
    context: ComponentContext,
) : Screen(context) {
    private val viewModel = viewModel { viewModelFactory.create() }

    @Composable
    override fun Render(modifier: Modifier) = HomeScreenContent(viewModel)
}
