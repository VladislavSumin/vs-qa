package ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.dragAndDrop.DragAndDropOverlay
import ru.vladislavsumin.core.ui.dragAndDrop.rememberDragAndDropFilesTarget
import ru.vladislavsumin.core.ui.filePicker.FilePickerDialog
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.Res
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_drop_logs_here
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_legal_info
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_open_logs_hint
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_open_new_file
import ru.vladislavsumin.feature.home_screen.impl.generated.resources.home_settings
import java.nio.file.Path

@Composable
internal fun HomeScreenContent(
    viewModel: HomeScreenViewModel,
    onLogPathsSelected: (List<Path>) -> Unit,
    logRecentComponent: ComposeComponent,
    adbDeviceListComponent: ComposeComponent?,
    onOpenLegalInfo: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    if (state) {
        FilePickerDialog(
            mimeType = "application/zip",
            multiple = true,
            onCloseRequest = viewModel::onOpenNewFilesDialogResult,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row {
            Box(Modifier.fillMaxHeight().weight(1f)) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.home_open_logs_hint),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::onClickOpen) { Text(stringResource(Res.string.home_open_new_file)) }
                    Spacer(Modifier.height(16.dp))
                    logRecentComponent.Render(Modifier.padding(16.dp))
                }
            }
            if (adbDeviceListComponent != null) {
                VerticalDivider(Modifier.fillMaxHeight(), color = QaTheme.colorScheme.background1)
                adbDeviceListComponent.Render(Modifier.fillMaxHeight().weight(1f).padding(16.dp))
            }
        }

        DragAndDropOverlay(modifier = Modifier.padding(32.dp)) {
            DropTargetCard(onLogPathsSelected)
        }

        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.home_legal_info),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(onClick = onOpenLegalInfo)
                    .padding(8.dp),
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(Res.string.home_settings),
                modifier = Modifier
                    .clickable(onClick = onOpenSettings)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun DropTargetCard(onLogPathsSelected: (List<Path>) -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    val borderColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxSize()
            .rememberDragAndDropFilesTarget(
                onDropped = onLogPathsSelected,
                onHoveredStateChanged = { isHovered = it },
            ),
        border = if (isHovered) BorderStroke(2.dp, borderColor) else null,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .defaultMinSize(
                    minWidth = 300.dp,
                    minHeight = 200.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(Res.string.home_drop_logs_here))
        }
    }
}
