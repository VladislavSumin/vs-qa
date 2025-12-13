package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

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
            items(items = state.devices, key = { it.name }) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {}
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = it.name,
                        Modifier.padding(end = 8.dp),
                    )
                    val color = when (it.statusColor) {
                        AdbDeviceListViewState.Device.StatusColor.Red -> QaTheme.colorScheme.logError.primary
                        AdbDeviceListViewState.Device.StatusColor.Yellow -> QaTheme.colorScheme.logWarn.primary
                        AdbDeviceListViewState.Device.StatusColor.Green -> QaTheme.colorScheme.logDebug.primary
                    }
                    Text(text = it.status, color = color)
                }
            }
        }
    }
}
