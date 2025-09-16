package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

/**
 * Компонент строки фильтра.
 *
 * @param onFocusLost вызывается при нажатии кнопки сброса фокуса с фильтра.
 * @param focusRequester объект через который можно вызвать фокус для фильтра.
 */
internal class FilterBarComponent(
    private val onFocusLost: () -> Boolean,
    private val focusRequester: FocusRequester,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val viewModel = viewModel { FilterBarViewModel() }

    val filterState get() = viewModel.filterState

    @Composable
    override fun Render(modifier: Modifier) = FilterBarContent(
        viewModel = viewModel,
        onFocusLost = onFocusLost,
        focusRequester = focusRequester,
        modifier = modifier,
    )
}
