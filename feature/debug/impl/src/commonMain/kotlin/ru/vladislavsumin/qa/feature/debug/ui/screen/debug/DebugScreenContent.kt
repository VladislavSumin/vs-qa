package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import kotlin.math.absoluteValue
import kotlin.random.Random

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
            Spacer(Modifier.weight(1f))
            val random = rememberSaveable { Random.nextInt().absoluteValue % 100 }
            val compositionRandom = Random.nextInt().absoluteValue % 100
            Text("STATE ${viewModel.random}_${random}_$compositionRandom")
        }
        Spacer(Modifier.height(8.dp))
        umlComponent.Render(Modifier.weight(1f))
    }
}
