package ru.vladislavsumin.core.ui.dashboardGrid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

internal class GridState(
    val columns: Int,
    val rows: Int,
    initialCellWidth: Dp,
    initialCellHeight: Dp,
    val density: Density,
    val isEditMode: Boolean,
    val collisionPadding: Int,
) {
    var cellWidth by mutableStateOf(initialCellWidth)
    var cellHeight by mutableStateOf(initialCellHeight)

    val cellWidthPx: Float get() = with(density) { cellWidth.toPx() }
    val cellHeightPx: Float get() = with(density) { cellHeight.toPx() }
    class Item(initialPlacement: GridPlacement) {
        var committedPlacement by mutableStateOf(initialPlacement)
    }

    val items = mutableStateMapOf<Any, Item>()

    data class DragSession(
        val itemKey: Any,
        val targetPlacement: GridPlacement,
    )

    var dragSession: DragSession? by mutableStateOf(null)
        private set

    private var resolvedCache: Map<Any, GridPlacement> by mutableStateOf(emptyMap())

    fun register(key: Any, placement: GridPlacement) {
        items[key] = Item(placement)
    }

    fun unregister(key: Any) {
        items.remove(key)
    }

    fun startDrag(key: Any) {
        val item = items[key] ?: return
        dragSession = DragSession(key, item.committedPlacement)
        resolvedCache = emptyMap()
    }

    fun updateDrag(key: Any, target: GridPlacement) {
        val session = dragSession ?: return
        if (session.itemKey != key) return
        dragSession = session.copy(targetPlacement = target)
        recomputeResolved(key, target)
    }

    fun endDrag(key: Any): GridPlacement {
        val session = dragSession
        dragSession = null
        resolvedCache = emptyMap()
        return session?.targetPlacement
            ?: items[key]?.committedPlacement
            ?: GridPlacement(0, 0, 1, 1)
    }

    fun cancelDrag(key: Any) {
        if (dragSession?.itemKey != key) return
        dragSession = null
        resolvedCache = emptyMap()
    }

    fun resolve(key: Any): GridPlacement? {
        val session = dragSession
        if (session != null && key == session.itemKey) {
            return session.targetPlacement
        }
        return resolvedCache[key] ?: items[key]?.committedPlacement
    }

    private fun recomputeResolved(draggedKey: Any, target: GridPlacement) {
        val result = LinkedHashMap<Any, GridPlacement>()
        for ((key, item) in items) {
            result[key] = item.committedPlacement
        }
        result[draggedKey] = target

        val queue = ArrayDeque<Any>()
        val visited = mutableSetOf<Any>()
        queue.add(draggedKey)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            val currentPos = result[current] ?: continue

            for ((key, pos) in result.toMap()) {
                if (key == current || key in visited) continue
                if (overlaps(currentPos, pos)) {
                    val others = result.filterKeys { it != key }.values.toList()
                    val pushed = pushAway(pos, currentPos, others)
                    if (pushed != pos) {
                        result[key] = pushed
                        queue.add(key)
                    }
                }
            }
        }

        resolvedCache = result.toMap()
    }

    private fun overlaps(a: GridPlacement, b: GridPlacement): Boolean {
        val p = collisionPadding
        return a.column < b.column + b.width + p &&
            a.column + a.width + p > b.column &&
            a.row < b.row + b.height + p &&
            a.row + a.height + p > b.row
    }

    private fun pushAway(
        item: GridPlacement,
        obstacle: GridPlacement,
        others: List<GridPlacement>,
    ): GridPlacement {
        val p = collisionPadding

        val rightCol = obstacle.column + obstacle.width + p
        val leftCol = obstacle.column - item.width - p
        val downRow = obstacle.row + obstacle.height + p
        val upRow = obstacle.row - item.height - p

        val overlapRight = item.column + item.width + p - obstacle.column
        val overlapLeft = obstacle.column + obstacle.width + p - item.column
        val xOverlap = minOf(overlapRight, overlapLeft)

        val overlapDown = item.row + item.height + p - obstacle.row
        val overlapUp = obstacle.row + obstacle.height + p - item.row
        val yOverlap = minOf(overlapDown, overlapUp)

        val directions = if (xOverlap <= yOverlap) {
            listOf(
                GridPlacement(rightCol.coerceAtLeast(0), item.row, item.width, item.height),
                GridPlacement(leftCol.coerceAtLeast(0), item.row, item.width, item.height),
                GridPlacement(item.column, downRow.coerceAtLeast(0), item.width, item.height),
                GridPlacement(item.column, upRow.coerceAtLeast(0), item.width, item.height),
            )
        } else {
            listOf(
                GridPlacement(item.column, downRow.coerceAtLeast(0), item.width, item.height),
                GridPlacement(item.column, upRow.coerceAtLeast(0), item.width, item.height),
                GridPlacement(rightCol.coerceAtLeast(0), item.row, item.width, item.height),
                GridPlacement(leftCol.coerceAtLeast(0), item.row, item.width, item.height),
            )
        }

        for (candidate in directions) {
            if (!isOutOfBounds(candidate) && !others.any { overlaps(candidate, it) }) {
                return candidate
            }
        }

        return item
    }

    private fun isOutOfBounds(p: GridPlacement): Boolean {
        return p.column < 0 ||
            p.row < 0 ||
            p.column + p.width > columns ||
            p.row + p.height > rows
    }
}
