package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog

@Composable
internal fun HomeScreenContent(viewModel: HomeScreenViewModel) {
    val state by viewModel.state.collectAsState()
    if (state) FilePickerDialog(mimeType = "application/zip", onCloseRequest = viewModel::onOpenNewFileDialogResult)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "All tabs closed\nPress Command + O for open new one",
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::onClickOpen) { Text("Open new file") }
        }
    }
}
