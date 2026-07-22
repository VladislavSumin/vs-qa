package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import androidx.compose.runtime.Stable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.decompose.components.ViewModel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Stable
internal class BottomBarUiInteractorImpl :
    ViewModel(),
    BottomBarUiInteractor {
    val additionalText = MutableStateFlow<BottomBarText?>(null)

    private val lock = ReentrantLock()

    @Volatile
    private var progressBarSequence = 0
    private val progressBarTexts: LinkedHashMap<Int, BottomBarText> = LinkedHashMap<Int, BottomBarText>()
    val progressBarState = MutableStateFlow<BottomBarText?>(null)

    override suspend fun showProgressBar(text: BottomBarText): Nothing {
        val id = lock.withLock {
            val id = progressBarSequence++
            progressBarTexts[id] = text
            updateCurrentProgressBar()
            id
        }
        try {
            delay(Long.MAX_VALUE)
            error("Unreachable")
        } finally {
            lock.withLock {
                progressBarTexts.remove(id)
                updateCurrentProgressBar()
            }
        }
    }

    private fun updateCurrentProgressBar() {
        val text = progressBarTexts.entries.firstOrNull()?.component2()
        progressBarState.value = text
    }

    override fun setBottomBarText(text: BottomBarText) {
        additionalText.value = text
    }
}
