package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

@Composable
expect fun VsVerticalScrollbar(lazyListState: LazyListState)
