package ru.vladislavsumin.core.ui.dashboardGrid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

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

@Composable
fun DashboardGrid(
    columns: Int,
    rows: Int,
    isEditMode: Boolean = false,
    collisionPadding: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable DashboardGridScope.() -> Unit,
) {
    require(columns > 0) { "columns must be > 0, was $columns" }
    require(rows > 0) { "rows must be > 0, was $rows" }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows

        val gridState = remember(columns, rows, isEditMode, collisionPadding, density) {
            GridState(
                columns = columns,
                rows = rows,
                initialCellWidth = cellWidth,
                initialCellHeight = cellHeight,
                density = density,
                isEditMode = isEditMode,
                collisionPadding = collisionPadding,
            )
        }

        gridState.cellWidth = cellWidth
        gridState.cellHeight = cellHeight

        Box(Modifier.fillMaxSize()) {
            if (isEditMode) {
                GridOverlay(
                    columns = columns,
                    rows = rows,
                )
            }
            val scope = remember(gridState) { DashboardGridScopeImpl(gridState) }
            scope.content()
        }
    }
}
