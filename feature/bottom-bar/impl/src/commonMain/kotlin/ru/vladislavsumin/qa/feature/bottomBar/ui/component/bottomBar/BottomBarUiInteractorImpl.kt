package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import androidx.compose.runtime.Stable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.decompose.components.ViewModel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Stable
internal class BottomBarUiInteractorImpl : ViewModel(), BottomBarUiInteractor {
    val additionalText = MutableStateFlow("")

    private val lock = ReentrantLock()

    @Volatile
    private var progressBarSequence = 0
    private val progressBarTexts: LinkedHashMap<Int, String> = LinkedHashMap<Int, String>()
    val progressBarState = MutableStateFlow<String?>(null)

    override suspend fun showProgressBar(text: String): Nothing {
        // TODO ерунда если честно, нужно потом переписать
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

    override fun setBottomBarText(text: String) {
        additionalText.value = text
    }
}
