package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.dragAndDrop.rememberDragAndDropFilesTarget
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog
import java.nio.file.Path

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun HomeScreenContent(
    viewModel: HomeScreenViewModel,
    onLogPathsSelected: (List<Path>) -> Unit,
    logRecentComponent: ComposeComponent,
    adbDeviceListComponent: ComposeComponent?,
) {
    val state by viewModel.state.collectAsState()
    if (state) FilePickerDialog(mimeType = "application/zip", onCloseRequest = viewModel::onOpenNewFileDialogResult)

    var isShowDragAndDropActions by remember { mutableStateOf(false) }
    val rootDragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isShowDragAndDropActions = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isShowDragAndDropActions = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    Spacer(Modifier.height(16.dp))
                    logRecentComponent.Render(Modifier)
                }
            }
            if (adbDeviceListComponent != null) {
                VerticalDivider(Modifier.fillMaxHeight())
                adbDeviceListComponent.Render(Modifier.fillMaxHeight().weight(1f))
            }
        }

        Box(
            modifier = Modifier
                .let { if (isShowDragAndDropActions) it.fillMaxSize().padding(32.dp) else it.size(0.dp) }
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = rootDragAndDropTarget,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Card(modifier = Modifier.rememberDragAndDropFilesTarget(onLogPathsSelected)) {
                Box(
                    Modifier.defaultMinSize(
                        minWidth = 300.dp,
                        minHeight = 200.dp,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Drop logs here")
                }
            }
        }
    }
}
