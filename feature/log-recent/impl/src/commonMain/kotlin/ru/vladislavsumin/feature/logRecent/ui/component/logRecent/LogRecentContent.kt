package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
internal fun LogRecentContent(
    viewModel: LogRecentViewModel,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    LazyColumn(modifier = modifier) {
        items(state.recents, key = { it.path.toString() }) {
            Text(it.path.toString())
        }
    }
}
