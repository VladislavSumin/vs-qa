package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.runtime.Stable
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import kotlin.math.absoluteValue
import kotlin.random.Random

@GenerateFactory
@Stable
internal class DebugScreenViewModel : ViewModel() {
    // Для визуального контроля пересоздания вьюмодели
    val random = Random.nextInt().absoluteValue % 100

    @Suppress("TooGenericExceptionThrown")
    fun onClickCrash(): Unit = throw RuntimeException("Test crash from debug screen")
}
