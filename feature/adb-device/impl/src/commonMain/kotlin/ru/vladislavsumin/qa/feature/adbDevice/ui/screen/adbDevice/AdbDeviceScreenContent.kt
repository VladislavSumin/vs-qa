package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun AdbDeviceScreenContent(deviceName: String, modifier: Modifier) {
    Box(Modifier.fillMaxSize().then(modifier)) {
        Text(
            text = "Device: $deviceName",
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
