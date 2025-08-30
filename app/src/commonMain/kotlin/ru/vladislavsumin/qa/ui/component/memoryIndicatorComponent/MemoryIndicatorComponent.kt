package ru.vladislavsumin.qa.ui.component.memoryIndicatorComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

class MemoryIndicatorComponent(context: ComponentContext) : Component(context), ComposeComponent {

    private val state: StateFlow<Pair<Long, Long>> = flow {
        val runtime = Runtime.getRuntime()
        while (true) {
            val total = runtime.totalMemory() / 1024 / 1024
            val used = total - runtime.freeMemory() / 1024 / 1024
            emit(used to total)
            delay(500L)
        }
    }.stateIn(scope, started = SharingStarted.Eagerly, initialValue = 1L to 1L)

    @Composable
    override fun Render(modifier: Modifier) {
        Box(
            Modifier
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
        ) {
            val state by state.collectAsState()
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .5f))
                    .fillMaxHeight()
                    .fillMaxWidth(state.first.toFloat() / state.second)
            )
            Text(
                text = "${state.first} of ${state.second}mb",
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(
                    vertical = 2.dp,
                    horizontal = 8.dp,
                )
            )
        }
    }
}