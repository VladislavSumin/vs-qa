package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.TextSelectionSeparator
import ru.vladislavsumin.feature.logViewer.ui.utils.colorize

internal fun LazyListScope.logItems(
    logs: List<LogsViewState.SectionInfo>,
    showRunNumbers: Boolean,
    useStickyHeaders: Boolean = true,
    header: @Composable (index: Int, runNumber: Int, meta: Map<String, String>?) -> Unit,
    record: @Composable (index: Int, log: LogRecord) -> Unit,
) {
    var itemIndex = 0
    logs.forEachIndexed { runNumber, sectionInfo ->
        if (showRunNumbers) {
            val headerIndex = itemIndex
            if (useStickyHeaders) {
                stickyHeader(key = -runNumber - 1) {
                    header(headerIndex, runNumber + 1, sectionInfo.meta)
                }
            } else {
                item(key = -runNumber - 1) {
                    header(headerIndex, runNumber + 1, sectionInfo.meta)
                }
            }
            itemIndex++
        }
        items(sectionInfo.logs, { it.order.value }) { log ->
            record(itemIndex, log)
            itemIndex++
        }
    }
}

@Composable
internal fun LogHeader(
    runNumber: Int,
    meta: Map<String, String>?,
    fontSize: Int,
    textSizeDp: Dp,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(QaTheme.colorScheme.surfaceVariant),
    ) {
        val text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Run $runNumber  ")
            }
            meta?.forEach { (k, v) ->
                withStyle(SpanStyle(color = QaTheme.colorScheme.logTrace.primary)) {
                    append(k)
                    append("=")
                }
                append(v)
                append(" ")
            }
        }
        Text(
            text = text,
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp * 1.42,
            modifier = Modifier.padding(start = textSizeDp + 13.dp + 8.dp, end = 4.dp),
        )
    }
}

@Composable
internal fun LogRecord(
    log: LogRecord,
    isSelected: Boolean,
    stripDate: Boolean,
    fontSize: Int,
    textSizeDp: Dp,
) {
    Box {
        DisableSelection {
            Text(
                text = "${log.order.value + 1}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = fontSize.sp,
                lineHeight = fontSize.sp * 1.42,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .defaultMinSize(minWidth = textSizeDp),
            )
        }
        Text(
            text = log.colorize(isSelected, stripDate),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp * 1.42,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = textSizeDp + 13.dp),
        )
        TextSelectionSeparator()
    }
}

@Composable
internal fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}
