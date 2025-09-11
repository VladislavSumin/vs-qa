package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.decompose.components.ViewModel

@Stable
internal class BottomBarUiInteractorImpl : ViewModel(), BottomBarUiInteractor {
    val additionalText = MutableStateFlow("")

    override fun setBottomBarText(text: String) {
        additionalText.value = text
    }
}
