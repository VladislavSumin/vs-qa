package ru.vladislavsumin.qa.feature.debug.ui.screen.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vladislavsumin.core.ui.dashboardGrid.DashboardGrid
import ru.vladislavsumin.core.ui.dashboardGrid.GridPlacement
import ru.vladislavsumin.core.ui.icons.QaIcons

private val CARD_COLORS = listOf(
    Color(0xFFE57373), // red
    Color(0xFF64B5F6), // blue
    Color(0xFF81C784), // green
    Color(0xFFFFB74D), // orange
    Color(0xFFBA68C8), // purple
    Color(0xFF4DB6AC), // teal
    Color(0xFFF06292), // pink
    Color(0xFF7986CB), // indigo
)

private val INITIAL_PLACEMENTS = listOf(
    GridPlacement(column = 0, row = 0, width = 5, height = 4),
    GridPlacement(column = 6, row = 0, width = 4, height = 3),
    GridPlacement(column = 11, row = 0, width = 5, height = 5),
    GridPlacement(column = 17, row = 0, width = 3, height = 4),
    GridPlacement(column = 0, row = 5, width = 8, height = 5),
    GridPlacement(column = 9, row = 5, width = 5, height = 4),
    GridPlacement(column = 15, row = 5, width = 5, height = 6),
    GridPlacement(column = 2, row = 11, width = 7, height = 4),
)

@Composable
@Suppress("LongMethod")
internal fun DashboardDemoScreenContent(modifier: Modifier = Modifier) {
    val placements = remember { mutableStateListOf<GridPlacement>().also { it.addAll(INITIAL_PLACEMENTS) } }
    var isEditMode by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { isEditMode = !isEditMode }) {
                Text(if (isEditMode) "Exit Edit Mode" else "Edit Mode")
            }
            if (isEditMode) {
                Spacer(Modifier.padding(horizontal = 4.dp))
                Button(
                    onClick = {
                        repeat(maxOf(placements.size, INITIAL_PLACEMENTS.size)) { i ->
                            if (i < INITIAL_PLACEMENTS.size && i < placements.size) {
                                placements[i] = INITIAL_PLACEMENTS[i]
                            } else if (i < INITIAL_PLACEMENTS.size) {
                                placements.add(INITIAL_PLACEMENTS[i])
                            } else {
                                placements.removeAt(placements.lastIndex)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text("Reset")
                }
            }
            Spacer(Modifier.weight(1f))
            if (isEditMode) {
                Button(onClick = {
                    val newCol = (0..18).random()
                    val newRow = (0..12).random()
                    placements.add(GridPlacement(column = newCol, row = newRow, width = 2, height = 2))
                }) {
                    Icon(QaIcons.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Add Card")
                }
            }
            Spacer(Modifier.padding(end = 4.dp))
            Text(
                text = "${placements.size} cards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        DashboardGrid(
            columns = 20,
            rows = 15,
            isEditMode = isEditMode,
            collisionPadding = 0,
            modifier = Modifier.fillMaxSize(),
        ) {
            placements.forEachIndexed { index, placement ->
                GridItem(
                    placement = placement,
                    onMove = if (isEditMode) {
                        { placements[index] = it }
                    } else {
                        null
                    },
                    onResize = if (isEditMode) {
                        { placements[index] = it }
                    } else {
                        null
                    },
                ) {
                    val color = CARD_COLORS[index % CARD_COLORS.size]
                    Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(6.dp))) {
                        if (isEditMode) {
                            IconButton(
                                onClick = { placements.removeAt(index) },
                                modifier = Modifier.align(Alignment.TopEnd),
                            ) {
                                Icon(
                                    QaIcons.Close,
                                    contentDescription = "Remove card",
                                    tint = Color.White,
                                )
                            }
                        }
                        Text(
                            text = "Card ${index + 1}",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
