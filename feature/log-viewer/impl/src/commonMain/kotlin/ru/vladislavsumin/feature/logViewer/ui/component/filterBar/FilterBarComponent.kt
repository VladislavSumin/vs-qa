package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

/**
 * Компонент строки фильтра.
 *
 * @param onFocusLost вызывается при нажатии кнопки сброса фокуса с фильтра.
 * @param focusRequester объект через который можно вызвать фокус для фильтра.
 */
@GenerateFactory
internal class FilterBarComponent(
    private val viewModelFactory: FilterBarViewModelFactory,
    @ByCreate context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel = viewModel { viewModelFactory.create() }

    val filterBarUiInteractor = viewModel
    private val focusRequester = FocusRequester()

    init {
        launch {
            viewModel.events.receiveAsFlow().collect { event ->
                when (event) {
                    FilterBarEvent.Focus -> focusRequester.requestFocus()
                }
            }
        }
    }

    @Composable
    override fun Render(modifier: Modifier) = FilterBarContent(
        viewModel = viewModel,
        focusRequester = focusRequester,
        modifier = modifier,
    )
}
