package ru.vladislavsumin.core.ui.dashboardGrid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface DashboardGridScope {
    @Composable
    fun GridItem(
        placement: GridPlacement,
        modifier: Modifier = Modifier,
        onMove: ((GridPlacement) -> Unit)? = null,
        onResize: ((GridPlacement) -> Unit)? = null,
        content: @Composable () -> Unit,
    )
}
