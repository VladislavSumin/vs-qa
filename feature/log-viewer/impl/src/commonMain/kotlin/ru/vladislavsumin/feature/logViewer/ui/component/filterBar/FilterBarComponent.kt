package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo
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
    viewModelFactory: FilterBarViewModelFactory,
    filterHintComponentFactory: FilterHintComponentFactory,
    @ByCreate currentTags: Flow<Set<String>>,
    @ByCreate currentRuns: Flow<List<RunIdInfo>>,
    @ByCreate globalHotkeyManager: GlobalHotkeyManager,
    @ByCreate context: ComponentContext,
) : Component(context),
    ComposeComponent {
    private val viewModel: FilterBarViewModel = viewModel { viewModelFactory.create() }

    private val filterHintComponent = filterHintComponentFactory.create(
        currentTokenPrediction = viewModel.filterState.map { it.currentTokenPredictionInfo },
        currentTags = currentTags,
        currentRuns = currentRuns,
        context = context.childContext("filter-hint"),
    )

    val filterBarUiInteractor: FilterBarUiInteractor = viewModel
    private val focusRequester = FocusRequester()

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            globalHotkeyManager.subscribe(
                KeyModifier.Command + KeyModifier.Shift + Key.F to {
                    focusRequester.requestFocus()
                    true
                },
            )
        }
        launch {
            viewModel.events.receiveAsFlow().collect { event ->
                when (event) {
                    FilterBarEvent.RequestShowHint -> filterHintComponent.filterHintUiInteractor.requestShow()
                }
            }
        }
        launch {
            filterHintComponent.filterHintUiInteractor.events.receiveAsFlow().collect { event ->
                when (event) {
                    is FilterHintUiInteractor.Event.ReplaceText -> viewModel.replaceFilterText(
                        event.removeLen,
                        event.text,
                    )
                }
            }
        }
    }

    @Composable
    override fun Render(modifier: Modifier) = FilterBarContent(
        viewModel = viewModel,
        filterHintComponent = filterHintComponent,
        filterHintHotkeyController = filterHintComponent.hotkeyController,
        focusRequester = focusRequester,
        modifier = modifier,
    )
}
