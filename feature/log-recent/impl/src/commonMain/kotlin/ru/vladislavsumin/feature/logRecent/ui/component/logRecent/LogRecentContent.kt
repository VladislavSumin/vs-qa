package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.hint.hint
import ru.vladislavsumin.feature.logRecent.domain.LogRecent
import java.nio.file.Path

@Composable
internal fun LogRecentContent(
    onOpenLogRecent: (path: Path) -> Unit,
    viewModel: LogRecentViewModel,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    LazyColumn(modifier = modifier.sizeIn(maxWidth = 600.dp)) {
        items(state.recents, key = { it.path.toString() }) { recentLog ->
            LogRecentItem(
                recentLog = recentLog,
                onOpenLogRecent = onOpenLogRecent,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod") // TODO починить говнокод от ии
private fun LogRecentItem(
    recentLog: LogRecent,
    onOpenLogRecent: (path: Path) -> Unit,
    viewModel: LogRecentViewModel,
) {
    val displayName = recentLog.customName?.takeIf { it.isNotBlank() }
    var editing by remember { mutableStateOf(false) }
    var editText by remember {
        mutableStateOf(
            TextFieldValue(
                displayName ?: recentLog.path.toString(),
            ),
        )
    }
    val focusRequester = remember { FocusRequester() }

    Row(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (editing) {
                    Modifier
                } else {
                    Modifier.clickable {
                        if (viewModel.checkRecentCanBeOpened(recentLog)) {
                            onOpenLogRecent(recentLog.path)
                        }
                    }
                },
            )
            .padding(vertical = 2.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editing) {
            val textColor = MaterialTheme.colorScheme.onSurface
            BasicTextField(
                value = editText,
                onValueChange = { editText = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyUp) {
                            when (event.key) {
                                Key.Enter -> {
                                    commitRename(recentLog, editText.text, viewModel)
                                    editing = false
                                    true
                                }

                                Key.Escape -> {
                                    editing = false
                                    true
                                }

                                else -> false
                            }
                        } else {
                            false
                        }
                    },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = textColor),
                cursorBrush = SolidColor(textColor),
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            Column(Modifier.weight(1f)) {
                Text(
                    text = displayName ?: recentLog.path.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (displayName != null) {
                    Text(
                        text = recentLog.path.toString(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        QaIconButton(
            onClick = {
                if (editing) {
                    commitRename(recentLog, editText.text, viewModel)
                    editing = false
                } else {
                    editText = TextFieldValue(
                        text = displayName ?: recentLog.path.toString(),
                        selection = TextRange(0, (displayName ?: recentLog.path.toString()).length),
                    )
                    editing = true
                }
            },
            modifier = Modifier.hint("Rename"),
        ) {
            Icon(
                if (editing) Icons.Outlined.Save else Icons.Outlined.Edit,
                contentDescription = null,
            )
        }

        QaIconButton(
            onClick = { viewModel.onClickRemoveRecent(recentLog) },
            modifier = Modifier.hint("Remove from recent"),
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = null)
        }
    }
}

private fun commitRename(recentLog: LogRecent, text: String, viewModel: LogRecentViewModel) {
    viewModel.renameRecent(recentLog, text.trim().ifBlank { null })
}
