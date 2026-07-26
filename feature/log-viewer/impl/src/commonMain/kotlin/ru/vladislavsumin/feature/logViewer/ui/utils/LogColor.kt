package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.core.ui.textHighlight.highlightBackground
import ru.vladislavsumin.feature.logParser.domain.LogRange
import ru.vladislavsumin.feature.logParser.domain.substring
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord
import kotlin.math.abs

@Composable
fun LogRecord.colorize(isSelected: Boolean, stripDate: Boolean): AnnotatedString {
    val logColor = LevelColors.getLevelColor(logLevel)
    val tagColors = QaTheme.colorScheme.tagColors
    val tagText = raw.substring(tag)
    val tagColor = tagColors[abs(tagText.hashCode()) % tagColors.size]
    val result = buildAnnotatedString {
        append(raw)
        addStyle(SpanStyle(color = QaTheme.colorScheme.content2), time)
        highlightBackground(level.first..level.last, logColor.background, logColor.onBackground)
        addStyle(SpanStyle(color = QaTheme.colorScheme.content1, fontStyle = FontStyle.Italic), thread)
        processName?.let {
            addStyle(SpanStyle(color = QaTheme.colorScheme.content2, fontStyle = FontStyle.Italic), it)
        }
        addStyle(SpanStyle(color = tagColor, fontWeight = FontWeight.W600), tag)
        addStyle(SpanStyle(color = logColor.primary), message)
        searchHighlights?.forEach { index ->
            addStyle(
                SpanStyle(
                    background = if (isSelected) {
                        QaTheme.colorScheme.logHighlightSelected
                    } else {
                        QaTheme.colorScheme.logHighlight
                    },
                ),
                index,
            )
        }
    }
    return if (stripDate) {
        // TODO это работает только для даты расположенной в начале записи
        check(timeDate.first == 0) { "timeDate not at start, not supported now!" }
        result.subSequence(timeDate.last + 2, raw.length)
    } else {
        result
    }
}

fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: IntRange) {
    addStyle(style, range.first, range.last + 1)
}

fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: LogRange) {
    addStyle(style, range.start, range.endInclusive + 1)
}
