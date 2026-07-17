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

internal class DashboardGridScopeImpl(val gridState: GridState) : DashboardGridScope {
    @Composable
    override fun GridItem(
        placement: GridPlacement,
        modifier: Modifier,
        onMove: ((GridPlacement) -> Unit)?,
        onResize: ((GridPlacement) -> Unit)?,
        content: @Composable () -> Unit,
    ) {
        DashboardGridItem(
            placement = placement,
            modifier = modifier,
            onMove = onMove,
            onResize = onResize,
            content = content,
        )
    }
}
