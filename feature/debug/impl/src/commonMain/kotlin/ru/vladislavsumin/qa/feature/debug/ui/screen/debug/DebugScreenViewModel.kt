package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.runtime.Stable
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory
@Stable
internal class DebugScreenViewModel : ViewModel() {
    @Suppress("TooGenericExceptionThrown")
    fun onClickCrash(): Unit = throw RuntimeException("Test crash from debug screen")
}
