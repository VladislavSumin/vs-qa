package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.Flow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.ui.hotkeyController.HotkeyController
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.logViewer.domain.logs.RunIdInfo

@GenerateFactory
internal class FilterHintComponent(
    viewModelFactory: FilterHintViewModelFactory,
    @ByCreate currentTokenPrediction: Flow<CurrentTokenPrediction?>,
    @ByCreate currentTags: Flow<Set<String>>,
    @ByCreate currentRuns: Flow<List<RunIdInfo>>,
    @ByCreate context: ComponentContext,
) : Component(context) {
    private val viewModel: FilterHintViewModel = viewModel {
        viewModelFactory.create(currentTokenPrediction, currentTags, currentRuns)
    }

    val filterHintUiInteractor: FilterHintUiInteractor get() = viewModel

    /**
     * Контроллер нажатий для работы с попапом, должен подключаться к внешнему элементу который находится в фокусе
     * при открытии попапа и может корректно перехватывать нажатия.
     */
    val hotkeyController = HotkeyController(
        KeyModifier.None + Key.DirectionDown to {
            viewModel.onSelectNextItem()
            true
        },
        KeyModifier.None + Key.DirectionUp to {
            viewModel.onSelectPrevItem()
            true
        },

        KeyModifier.None + Key.Escape to {
            viewModel.onCloseRequest()
        },
        KeyModifier.Control + Key.Spacebar to {
            // TODO какой то баг в compose хоткей отрабатывает но пробел все равно добавляется к полю.
            viewModel.onShowRequest()
            true
        },

        KeyModifier.None + Key.Enter to {
            viewModel.onAcceptCurrentHint()
        },
    )

    /**
     * @param offsetX смещение по оси х в пикселях на которое нужно сдвинуть подсказку относительно ее базового
     * контейнера.
     */
    @Composable
    fun Render(modifier: Modifier, offsetX: Float) = FilterHintContent(viewModel, modifier, offsetX)
}
