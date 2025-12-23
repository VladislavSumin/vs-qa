package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.button.QaIconButton
import java.nio.file.Path

@Composable
internal fun LogRecentContent(
    onOpenLogRecent: (path: Path) -> Unit,
    viewModel: LogRecentViewModel,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    LazyColumn(modifier = modifier) {
        items(state.recents, key = { it.path.toString() }) { recentLog ->
            Row(
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenLogRecent(recentLog.path) }
                    .padding(vertical = 2.dp, horizontal = 4.dp),
            ) {
                Text(recentLog.path.toString())
                QaIconButton(onClick = { viewModel.onClickRemoveRecent(recentLog) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }
    }
}
