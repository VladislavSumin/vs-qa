package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AdbDeviceScreenContent(deviceName: String, viewModel: DeviceControlViewModel, modifier: Modifier) {
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().then(modifier).verticalScroll(rememberScrollState())) {
        Text(
            text = "Device: $deviceName",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when (val s = state) {
            DeviceControlViewState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            }

            DeviceControlViewState.Error -> {
                Text(
                    "Failed to load parameters",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                )
            }

            is DeviceControlViewState.Content -> {
                for (parameter in s.parameters) {
                    when (parameter) {
                        is DeviceParameter.Toggle -> ParameterToggleRow(parameter) { value ->
                            viewModel.onToggle(parameter.id, value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParameterToggleRow(parameter: DeviceParameter.Toggle, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = parameter.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = parameter.isChecked,
            enabled = !parameter.isLoading,
            onCheckedChange = onToggle,
        )
    }
}
