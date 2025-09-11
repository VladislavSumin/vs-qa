package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.qa.feature.memoryIndicator.ui.component.memoryIndicator.MemoryIndicatorComponentFactory

internal class BottomBarComponentImpl(
    memoryIndicatorComponentFactory: MemoryIndicatorComponentFactory,
    context: ComponentContext,
) : Component(context), BottomBarComponent {
    override val bottomBarUiInteractor: BottomBarUiInteractorImpl = viewModel { BottomBarUiInteractorImpl() }

    private val memoryIndicator = memoryIndicatorComponentFactory.create(context.childContext("memory-indicator"))

    @Composable
    override fun Render(modifier: Modifier) {
        Row(
            Modifier.background(QaTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f))
            Text(
                text = bottomBarUiInteractor.additionalText.collectAsState().value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(
                    vertical = 2.dp,
                    horizontal = 8.dp,
                ),
            )
            memoryIndicator.Render(Modifier)
        }
    }
}
