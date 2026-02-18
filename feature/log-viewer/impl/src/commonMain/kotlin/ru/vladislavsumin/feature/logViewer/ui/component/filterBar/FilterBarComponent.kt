package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logViewer.LinkedFlow
import ru.vladislavsumin.feature.logViewer.ui.component.filterHint.FilterHintComponentFactory
import ru.vladislavsumin.feature.logViewer.ui.component.filterHint.FilterHintUiInteractor

/**
 * Компонент строки фильтра.
 *
 * @param onFocusLost вызывается при нажатии кнопки сброса фокуса с фильтра.
 * @param focusRequester объект через который можно вызвать фокус для фильтра.
 */
@GenerateFactory
internal class FilterBarComponent(
    private val viewModelFactory: FilterBarViewModelFactory,
    filterHintComponentFactory: FilterHintComponentFactory,
    @ByCreate linkedBarState: LinkedFlow<FilterRequestParser.ParserResult>,
    @ByCreate context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel: FilterBarViewModel = viewModel { viewModelFactory.create(linkedBarState) }
    private val filterHintComponent = filterHintComponentFactory.create(
        currentTokenPrediction = viewModel.filterState.map { it.currentTokenPredictionInfo },
        context.childContext("filter-hint"),
    )

    val filterBarUiInteractor: FilterBarUiInteractor = viewModel
    private val focusRequester = FocusRequester()

    init {
        launch {
            viewModel.events.receiveAsFlow().collect { event ->
                when (event) {
                    FilterBarEvent.Focus -> focusRequester.requestFocus()
                    FilterBarEvent.RequestShowHint -> filterHintComponent.filterHintUiInteractor.requestShow()
                }
            }
        }
        launch {
            filterHintComponent.filterHintUiInteractor.events.receiveAsFlow().collect { event ->
                when (event) {
                    is FilterHintUiInteractor.Event.AppendText -> viewModel.appendFilterText(event.text)
                }
            }
        }
    }

    @Composable
    override fun Render(modifier: Modifier) = FilterBarContent(
        viewModel = viewModel,
        filterBarComponent = filterHintComponent,
        filterBarHotkeyController = filterHintComponent.hotkeyController,
        focusRequester = focusRequester,
        modifier = modifier,
    )
}
