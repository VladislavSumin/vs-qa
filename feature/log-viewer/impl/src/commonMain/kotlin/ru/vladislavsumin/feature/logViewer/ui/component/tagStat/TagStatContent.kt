package ru.vladislavsumin.feature.logViewer.ui.component.tagStat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.uikit.pieChart.PieChart
import ru.vladislavsumin.core.uikit.pieChart.Slice
import ru.vladislavsumin.feature.logViewer.ui.utils.LevelColors

@Composable
internal fun TagStatContent(
    viewModel: TagStatViewModel,
    modifier: Modifier,
) {
    val state by viewModel.state.collectAsState()
    LazyColumn(modifier) {
        items(state.tags, key = { it.tag }) { tagInfo ->
            Row {
                Text(tagInfo.recordCount.toString(), Modifier.defaultMinSize(minWidth = 64.dp))
                Spacer(Modifier.width(8.dp))
                val slices = tagInfo.levels.map {
                    Slice(fraction = it.second.toFloat(), LevelColors.getLevelColor(it.first).background)
                }
                PieChart(
                    slices,
                    Modifier
                        .padding(vertical = 2.dp)
                        .size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(tagInfo.tag)
            }
        }
    }
}
