package ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.feature.adb_device_list.impl.generated.resources.Res
import ru.vladislavsumin.feature.adb_device_list.impl.generated.resources.adb_device_list_dump_logs
import ru.vladislavsumin.feature.adb_device_list.impl.generated.resources.adb_device_list_error
import ru.vladislavsumin.feature.adb_device_list.impl.generated.resources.adb_device_list_title

@Composable
internal fun AdbDeviceListContent(
    onDeviceClick: (deviceName: String) -> Unit,
    onDumpLogsClick: (deviceName: String) -> Unit,
    viewModel: AdbDeviceListViewModel,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    when (val state = state) {
        is AdbDeviceListViewState.DeviceList -> DeviceList(state, onDeviceClick, onDumpLogsClick, modifier)

        AdbDeviceListViewState.Error -> {
            Text(stringResource(Res.string.adb_device_list_error))
        }
    }
}

@Composable
internal fun DeviceList(
    state: AdbDeviceListViewState.DeviceList,
    onDeviceClick: (deviceName: String) -> Unit,
    onDumpLogsClick: (deviceName: String) -> Unit,
    modifier: Modifier,
) {
    Box(modifier) {
        LazyColumn {
            item {
                Text(stringResource(Res.string.adb_device_list_title))
            }
            items(items = state.devices, key = { it.name }) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onDeviceClick(it.name) }
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { onDumpLogsClick(it.name) },
                        modifier = Modifier.hint(stringResource(Res.string.adb_device_list_dump_logs)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(Res.string.adb_device_list_dump_logs),
                        )
                    }
                }
            }
        }
    }
}
