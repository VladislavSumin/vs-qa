package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

@Composable
internal fun DebugScreenContent(
    viewModel: DebugScreenViewModel,
    umlComponent: ComposeComponent,
    onOpenDashboardDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::onClickCrash) {
                Text("Test crash")
            }
            Button(onClick = onOpenDashboardDemo) {
                Text("Dashboard Demo")
            }
        }
        Spacer(Modifier.height(8.dp))
        umlComponent.Render(Modifier.weight(1f))
    }
}
