package ru.vladislavsumin.qa.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ru.vladislavsumin.qa.domain.logs.RawLogRecord
import ru.vladislavsumin.qa.ui.theme.QaTheme

@Composable
fun RawLogRecord.colorize(): AnnotatedString {
    val (levelColor, onLevelColor) = LevelColors.getLevelColor(raw.substring(level))
    return buildAnnotatedString {
        append(raw)
        addStyle(SpanStyle(color = QaTheme.colorScheme.onSurfaceVariant), time)
        addStyle(SpanStyle(background = levelColor, color = onLevelColor), level)
        addStyle(SpanStyle(fontStyle = FontStyle.Italic), thread)
        addStyle(SpanStyle(color = levelColor, fontWeight = FontWeight.Bold), tag)
        addStyle(SpanStyle(color = levelColor), message)
        searchHighlight?.let { index ->
            addStyle(SpanStyle(background = QaTheme.colorScheme.onSurfaceVariant), index)
        }
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: IntRange) {
    addStyle(style, range.first, range.last + 1)
}
