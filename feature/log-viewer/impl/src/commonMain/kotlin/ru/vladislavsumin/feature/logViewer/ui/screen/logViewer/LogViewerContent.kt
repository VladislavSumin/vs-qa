package ru.vladislavsumin.feature.logViewer.ui.screen.logViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.button.QaToggleIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.hotkeyController.HotkeyController
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.feature.logViewer.ui.component.logs.LogsComponent
import ru.vladislavsumin.feature.logViewer.ui.component.searchBar.SearchBarContent
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Suppress("LongMethod") // TODO попилить
internal fun LogViewerContent(
    viewModel: LogViewerViewModel,
    rootFocusRequester: FocusRequester,
    filterFocusRequester: FocusRequester,
    filterBarComponent: ComposeComponent,
    logsComponent: LogsComponent,
    modifier: Modifier,
) {
    LaunchedEffect(rootFocusRequester) {
        rootFocusRequester.requestFocus()
    }
    val searchFocusRequester = remember { FocusRequester() }
    val hotkeyController = remember(filterFocusRequester, searchFocusRequester) {
        HotkeyController(
            KeyModifier.Shift + KeyModifier.Command + Key.F to { filterFocusRequester.requestFocus() },
            KeyModifier.Command + Key.F to { searchFocusRequester.requestFocus() },
        )
    }
    val dragAndDropTarget = rememberGlobalDragAndDropTarget(viewModel)
    Surface(
        modifier = modifier
            .focusRequester(rootFocusRequester)
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .onPreviewKeyEvent(hotkeyController::invoke)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget,
            ),
    ) {
        val state = viewModel.state.collectAsState()
        val searchState = derivedStateOf { state.value.searchState }
        Column {
            SearchBarContent(viewModel, searchState, searchFocusRequester, rootFocusRequester)
            Row(Modifier.weight(1f)) {
                logsComponent.Render(Modifier.weight(1f))
                SidePanelContent(viewModel, state)
            }
            filterBarComponent.Render(Modifier)
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = QaTheme.colorScheme.surface,
                thickness = 1.5.dp,
            )
        }

        Row(
            // Костыль, иначе не сможем получить событие drag && drop.
            Modifier.let {
                if (state.value.showDragAndDropContainers) {
                    it.fillMaxSize()
                } else {
                    it.size(0.dp)
                }
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val mappingDragAndDrop = remember(viewModel) {
                object : DragAndDropTarget {
                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        val files = event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        viewModel.onDragAndDropMappingFile(files.single())
                        return true
                    }
                }
            }

            val logsDragAndDrop = remember(viewModel) {
                object : DragAndDropTarget {
                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        val files = event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        viewModel.onDragAndDropLogsFile(files.single())
                        return true
                    }
                }
            }

            Card(
                modifier = Modifier
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = logsDragAndDrop,
                    ),
            ) {
                Box(
                    Modifier.defaultMinSize(
                        minWidth = 300.dp,
                        minHeight = 200.dp,
                    ),
                ) {
                    Text(
                        "Drop logs here",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }

            Card(
                modifier = Modifier
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { true },
                        target = mappingDragAndDrop,
                    ),
            ) {
                Box(
                    Modifier.defaultMinSize(
                        minWidth = 300.dp,
                        minHeight = 200.dp,
                    ),
                ) {
                    Text("Drop mapping here", Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun SidePanelContent(
    viewModel: LogViewerViewModel,
    state: State<LogViewerViewState>,
) {
    val clipboard = LocalClipboardManager.current
    Column(
        Modifier.fillMaxHeight().width(IntrinsicSize.Min).background(QaTheme.colorScheme.surfaceVariant),
    ) {
        Divider(color = QaTheme.colorScheme.surface, thickness = 1.5.dp)
        QaIconButton(
            onClick = {
                // TODO провести через вью модель.
                val data: String = state.value.logsViewState.logs.joinToString(separator = "\n") { it.raw }
                clipboard.setText(AnnotatedString(data))
            },
            Modifier.padding(4.dp),
        ) {
            Icon(Icons.Default.CopyAll, null)
        }
        if (state.value.showSelectMappingDialog) {
            FileDialog(onCloseRequest = viewModel::onSelectMappingDialogResult)
        }
        QaToggleIconButton(
            checked = state.value.isMappingApplied,
            onCheckedChange = { viewModel.onClickMappingButton() },
            Modifier.padding(4.dp),
        ) {
            Icon(Icons.Default.FilePresent, null)
        }
        Spacer(Modifier.weight(1f))
        Divider(color = QaTheme.colorScheme.surface, thickness = 1.5.dp)
    }
}

/**
 * Invisible text to separate text blocks in selected text.
 *
 * Workaround for [this issue](https://issuetracker.google.com/issues/285036739)
 */
@Composable
fun TextSelectionSeparator(text: String = "\n") {
    Text(
        modifier = Modifier.sizeIn(maxWidth = 0.dp, maxHeight = 0.dp),
        text = text,
    )
}

@Composable
private fun rememberGlobalDragAndDropTarget(viewModel: LogViewerViewModel): DragAndDropTarget {
    return remember(viewModel) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                viewModel.onStartDragAndDrop()
            }

            override fun onEnded(event: DragAndDropEvent) {
                viewModel.onStopDragAndDrop()
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                return false
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit,
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory + file)
                }
            }
        }
    },
    dispose = FileDialog::dispose,
)
