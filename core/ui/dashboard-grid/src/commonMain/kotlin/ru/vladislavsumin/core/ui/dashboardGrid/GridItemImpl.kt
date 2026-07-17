package ru.vladislavsumin.core.ui.dashboardGrid

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val DRAG_ALPHA = 0.7f

@Composable
@Suppress("LongMethod")
internal fun DashboardGridScopeImpl.DashboardGridItem(
    placement: GridPlacement,
    modifier: Modifier,
    onMove: ((GridPlacement) -> Unit)?,
    onResize: ((GridPlacement) -> Unit)?,
    content: @Composable () -> Unit,
) {
    val gridState = this.gridState
    val editMode = gridState.isEditMode
    val itemKey = remember { Any() }

    DisposableEffect(itemKey, gridState) {
        gridState.register(itemKey, placement)
        onDispose { gridState.unregister(itemKey) }
    }

    LaunchedEffect(placement, gridState) {
        gridState.items[itemKey]?.committedPlacement = placement
    }

    val resolved = gridState.resolve(itemKey) ?: placement
    val isDragging = gridState.dragSession?.itemKey == itemKey

    val animOffsetX by animateDpAsState(gridState.cellWidth * resolved.column)
    val animOffsetY by animateDpAsState(gridState.cellHeight * resolved.row)
    val animWidth by animateDpAsState(gridState.cellWidth * resolved.width)
    val animHeight by animateDpAsState(gridState.cellHeight * resolved.height)

    Box(
        modifier = Modifier
            .offset(animOffsetX, animOffsetY)
            .size(animWidth, animHeight)
            .padding(2.dp),
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (isDragging) {
                        Modifier.alpha(DRAG_ALPHA)
                    } else {
                        Modifier
                    },
                ),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (editMode) 4.dp else 0.dp,
            border = BorderStroke(
                width = 1.dp,
                color = if (editMode) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
            ),
        ) {
            Box(Modifier.padding(4.dp)) {
                content()
            }
        }

        if (editMode && onMove != null) {
            DashboardGridItemDragOverlay(
                gridState = gridState,
                itemKey = itemKey,
                onMove = onMove,
            )
        }

        if (editMode && onResize != null) {
            DashboardGridResizeHandle(
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd),
                gridState = gridState,
                itemKey = itemKey,
                onResize = onResize,
            )
        }
    }
}

@Composable
private fun DashboardGridItemDragOverlay(gridState: GridState, itemKey: Any, onMove: (GridPlacement) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDelta = Offset.Zero
                var startPlacement: GridPlacement? = null

                val dragCallbacks = object {
                    fun onStart() {
                        gridState.startDrag(itemKey)
                        startPlacement = gridState.items[itemKey]?.committedPlacement
                    }

                    fun onDrag(change: PointerInputChange, dragAmount: Offset) {
                        change.consume()
                        totalDelta += dragAmount
                        val sp = startPlacement ?: return
                        val dCol = (totalDelta.x / gridState.cellWidthPx).roundToInt()
                        val dRow = (totalDelta.y / gridState.cellHeightPx).roundToInt()
                        gridState.updateDrag(
                            itemKey,
                            sp.copy(
                                column = (sp.column + dCol).coerceIn(0, gridState.columns - sp.width),
                                row = (sp.row + dRow).coerceIn(0, gridState.rows - sp.height),
                            ),
                        )
                    }

                    fun onEnd() {
                        val final = gridState.endDrag(itemKey)
                        totalDelta = Offset.Zero
                        startPlacement = null
                        onMove(final)
                    }

                    fun onCancel() {
                        gridState.cancelDrag(itemKey)
                        totalDelta = Offset.Zero
                        startPlacement = null
                    }
                }

                if (useLongPressForDrag()) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragCallbacks.onStart() },
                        onDrag = { change, amount -> dragCallbacks.onDrag(change, amount) },
                        onDragEnd = dragCallbacks::onEnd,
                        onDragCancel = dragCallbacks::onCancel,
                    )
                } else {
                    detectDragGestures(
                        onDragStart = { dragCallbacks.onStart() },
                        onDrag = { change, amount -> dragCallbacks.onDrag(change, amount) },
                        onDragEnd = dragCallbacks::onEnd,
                        onDragCancel = dragCallbacks::onCancel,
                    )
                }
            },
    )
}

@Composable
private fun DashboardGridResizeHandle(
    modifier: Modifier,
    gridState: GridState,
    itemKey: Any,
    onResize: (GridPlacement) -> Unit,
) {
    Box(
        modifier = modifier
            .size(14.dp)
            .offset(x = 1.dp, y = 1.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                shape = CircleShape,
            )
            .pointerInput(Unit) {
                var totalDelta = Offset.Zero
                var startPlacement: GridPlacement? = null

                val dragCallbacks = object {
                    fun onStart() {
                        gridState.startDrag(itemKey)
                        startPlacement = gridState.items[itemKey]?.committedPlacement
                    }

                    fun onDrag(change: PointerInputChange, dragAmount: Offset) {
                        change.consume()
                        totalDelta += dragAmount
                        val sp = startPlacement ?: return
                        val dCol = (totalDelta.x / gridState.cellWidthPx).roundToInt()
                        val dRow = (totalDelta.y / gridState.cellHeightPx).roundToInt()
                        gridState.updateDrag(
                            itemKey,
                            sp.copy(
                                width = (sp.width + dCol).coerceIn(1, gridState.columns - sp.column),
                                height = (sp.height + dRow).coerceIn(1, gridState.rows - sp.row),
                            ),
                        )
                    }

                    fun onEnd() {
                        val final = gridState.endDrag(itemKey)
                        totalDelta = Offset.Zero
                        startPlacement = null
                        onResize(final)
                    }

                    fun onCancel() {
                        gridState.cancelDrag(itemKey)
                        totalDelta = Offset.Zero
                        startPlacement = null
                    }
                }

                if (useLongPressForDrag()) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragCallbacks.onStart() },
                        onDrag = { change, amount -> dragCallbacks.onDrag(change, amount) },
                        onDragEnd = dragCallbacks::onEnd,
                        onDragCancel = dragCallbacks::onCancel,
                    )
                } else {
                    detectDragGestures(
                        onDragStart = { dragCallbacks.onStart() },
                        onDrag = { change, amount -> dragCallbacks.onDrag(change, amount) },
                        onDragEnd = dragCallbacks::onEnd,
                        onDragCancel = dragCallbacks::onCancel,
                    )
                }
            },
    )
}
