package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

@Composable
internal fun LogPreview(
    state: LogsViewState,
    mainLazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val previewState = rememberLazyListState()
    val visibleRange = remember {
        derivedStateOf {
            val layoutInfo = mainLazyListState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                IntRange.EMPTY
            } else {
                val first = layoutInfo.visibleItemsInfo.firstOrNull { it.key as Int >= 0 }?.index ?: 0
                val last = layoutInfo.visibleItemsInfo.last().index
                first..last
            }
        }
    }

    PreviewAutoScroll(previewState, visibleRange)

    LazyColumn(
        state = previewState,
        modifier = modifier
            .width(120.dp)
            .fillMaxHeight()
            .clipToBounds(),
    ) {
        logItems(
            logs = state.logs,
            showRunNumbers = state.showRunNumbers,
            useStickyHeaders = false,
            header = { index, runNumber, _ ->
                PreviewHeader(index, runNumber, visibleRange.value)
            },
            record = { index, log ->
                PreviewRecord(index, log, state.stripDate, visibleRange.value)
            },
        )
    }
}

@Composable
private fun PreviewAutoScroll(
    previewState: LazyListState,
    visibleRange: androidx.compose.runtime.State<IntRange>,
) {
    LaunchedEffect(previewState) {
        snapshotFlow { visibleRange.value }
            .distinctUntilChanged()
            .collect { range ->
                if (range.isEmpty()) return@collect

                val totalItems = previewState.layoutInfo.totalItemsCount
                if (totalItems == 0) return@collect

                val layoutInfo = previewState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return@collect

                val viewportHeight = layoutInfo.viewportSize.height
                val firstItem = visibleItems.first()
                val lastItem = visibleItems.last()
                val visibleItemsHeight = lastItem.offset + lastItem.size - firstItem.offset
                if (visibleItemsHeight <= viewportHeight) return@collect

                val itemsAbove = range.first
                val highlightCount = range.last - range.first + 1
                val itemsBelow = totalItems - range.last - 1
                val totalAround = itemsAbove + itemsBelow
                if (totalAround == 0) return@collect

                val spaceInPreview = visibleItems.size - highlightCount
                if (spaceInPreview <= 0) {
                    previewState.scrollToItem(range.first.coerceAtMost(totalItems - 1))
                    return@collect
                }

                val spaceAbove = (spaceInPreview.toLong() * itemsAbove / totalAround).toInt()
                val targetFirstVisible = (range.first - spaceAbove).coerceIn(0, (totalItems - 1).coerceAtLeast(0))
                previewState.scrollToItem(targetFirstVisible)
            }
    }
}

@Composable
private fun PreviewHeader(
    index: Int,
    runNumber: Int,
    visibleRange: IntRange,
) {
    val isHighlighted = index in visibleRange
    val bgColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        QaTheme.colorScheme.surfaceVariant
    }
    Box(
        Modifier
            .fillMaxWidth()
            .background(bgColor),
    ) {
        val text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("R$runNumber")
            }
        }
        Text(
            text = text,
            fontSize = 1.sp,
            lineHeight = 1.5.sp,
            maxLines = 1,
            color = LocalContentColor.current,
            modifier = Modifier.padding(start = 1.dp, end = 1.dp),
        )
    }
}

@Composable
private fun PreviewRecord(
    index: Int,
    log: LogRecord,
    stripDate: Boolean,
    visibleRange: IntRange,
) {
    val isHighlighted = index in visibleRange
    val bgColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Box(
        Modifier
            .fillMaxWidth()
            .background(bgColor),
    ) {
        Text(
            text = log.colorize(isSelected = false, stripDate = stripDate),
            fontSize = 1.sp,
            lineHeight = 1.5.sp,
            maxLines = 1,
            color = LocalContentColor.current,
            modifier = Modifier.padding(start = 1.dp, end = 1.dp),
        )
    }
}
