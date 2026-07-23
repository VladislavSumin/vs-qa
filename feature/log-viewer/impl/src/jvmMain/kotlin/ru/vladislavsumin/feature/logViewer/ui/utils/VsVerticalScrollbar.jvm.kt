package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

@Composable
actual fun VsVerticalScrollbar(lazyListState: LazyListState, modifier: Modifier) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(lazyListState),
        style = LocalScrollbarStyle.current.copy(
            hoverColor = QaTheme.colorScheme.content1,
            unhoverColor = QaTheme.colorScheme.content2,
        ),
        modifier = modifier.fillMaxHeight(),
    )
}
