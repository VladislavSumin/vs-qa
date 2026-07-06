package ru.vladislavsumin.feature.logViewer.ui.component.savedFilters

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository

/**
 * Компонент панели сохраненных фильтров. Отвечает за отображение, создание, редактирование и удаление фильтров.
 *
 * Видимостью панели управляет родительский компонент.
 */
@GenerateFactory
internal class SavedFiltersComponent(
    viewModelFactory: SavedFiltersViewModelFactory,
    @ByCreate context: ComponentContext,
) : Component(context),
    ComposeComponent {
    private val viewModel: SavedFiltersViewModel = viewModel { viewModelFactory.create() }

    val savedFilters: StateFlow<List<SavedFiltersRepository.SavedFilter>> get() = viewModel.savedFilters

    /**
     * События клика по сохраненному фильтру. Передает имя фильтра для вставки в активную строку фильтра.
     */
    val filterClickEvents = Channel<String>(Channel.CONFLATED)

    @Composable
    override fun Render(modifier: Modifier) = SavedFiltersContent(
        viewModel = viewModel,
        modifier = modifier,
        onSavedFilterClick = { filterClickEvents.trySend(it) },
    )
}
