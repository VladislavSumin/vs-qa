@file:Suppress("INVISIBLE_REFERENCE")

package ru.vladislavsumin.core.ui.selection

import androidx.compose.foundation.text.selection.Selection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val CLICK_SELECTION_RESET_DELAY_MS = 500L

@Composable
fun VsSelectionContainer(
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var selection by remember { mutableStateOf<Selection?>(null) }
    onSelectionChange(selection != null && selection.start != selection.end)

    LaunchedEffect(selection) {
        if (selection != null && selection.start == selection.end) {
            delay(CLICK_SELECTION_RESET_DELAY_MS)
            selection = null
        }
    }

    SelectionContainer(
        modifier = modifier,
        selection = selection,
        onSelectionChange = { selection = it },
        children = content,
    )
}
