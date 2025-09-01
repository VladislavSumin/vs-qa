package ru.vladislavsumin.qa.ui.component.memoryIndicatorComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

class MemoryIndicatorComponent(context: ComponentContext) : Component(context), ComposeComponent {
    private val viewModel = viewModel { MemoryIndicatorViewModel() }

    @Composable
    override fun Render(modifier: Modifier) {
        Box(
            Modifier
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
                .clickable(onClick = viewModel::onClick),
        ) {
            val state by viewModel.state.collectAsState()
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .5f))
                    .fillMaxHeight()
                    .fillMaxWidth(state.first.toFloat() / state.second),
            )
            Text(
                text = "${state.first} of ${state.second}mb",
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(
                    vertical = 2.dp,
                    horizontal = 8.dp,
                ),
            )
        }
    }
}
