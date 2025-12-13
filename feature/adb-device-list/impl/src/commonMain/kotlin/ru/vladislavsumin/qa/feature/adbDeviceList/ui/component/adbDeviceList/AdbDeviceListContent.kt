package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
internal fun AdbDeviceListContent(
    viewModel: AdbDeviceListViewModel,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()

    Box(modifier) {
        LazyColumn {
            item {
                Text("Adb list")
            }
            items(items = state, key = { it.name }) {
                Row {
                    Text(it.name)
                    Text(it.status.name)
                }
            }
        }
    }
}
