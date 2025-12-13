package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog

@Composable
internal fun HomeScreenContent(
    viewModel: HomeScreenViewModel,
    adbDeviceListComponent: ComposeComponent?,
) {
    val state by viewModel.state.collectAsState()
    if (state) FilePickerDialog(mimeType = "application/zip", onCloseRequest = viewModel::onOpenNewFileDialogResult)

    Row {
        Box(Modifier.fillMaxHeight().weight(1f)) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Open logs\nPress Command + O for open new one",
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::onClickOpen) { Text("Open new file") }
            }
        }
        if (adbDeviceListComponent != null) {
            VerticalDivider(Modifier.fillMaxHeight())
            adbDeviceListComponent.Render(Modifier.fillMaxHeight().weight(1f))
        }
    }
}
