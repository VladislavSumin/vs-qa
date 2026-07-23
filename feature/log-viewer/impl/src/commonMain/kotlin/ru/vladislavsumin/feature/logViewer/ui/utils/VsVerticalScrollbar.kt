package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VsVerticalScrollbar(lazyListState: LazyListState, modifier: Modifier = Modifier)
