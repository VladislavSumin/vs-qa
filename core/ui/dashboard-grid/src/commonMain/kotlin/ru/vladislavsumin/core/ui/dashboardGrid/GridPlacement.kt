package ru.vladislavsumin.core.ui.dashboardGrid

data class GridPlacement(val column: Int, val row: Int, val width: Int, val height: Int) {
    init {
        require(column >= 0) { "column must be >= 0, was $column" }
        require(row >= 0) { "row must be >= 0, was $row" }
        require(width >= 1) { "width must be >= 1, was $width" }
        require(height >= 1) { "height must be >= 1, was $height" }
    }
}
