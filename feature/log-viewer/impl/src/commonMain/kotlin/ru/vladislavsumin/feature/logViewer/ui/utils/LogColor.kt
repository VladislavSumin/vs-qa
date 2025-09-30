package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.domain.logs.LogRecord

@Composable
fun LogRecord.colorize(isSelected: Boolean): AnnotatedString {
    val logColor = LevelColors.getLevelColor(logLevel)
    return buildAnnotatedString {
        append(raw)
        addStyle(SpanStyle(color = QaTheme.colorScheme.onSurfaceVariant), time)
        addStyle(SpanStyle(background = logColor.background, color = logColor.onBackground), level)
        addStyle(SpanStyle(fontStyle = FontStyle.Italic), thread)
        addStyle(SpanStyle(color = logColor.primary, fontWeight = FontWeight.Bold), tag)
        addStyle(SpanStyle(color = logColor.primary), message)
        searchHighlight?.let { index ->
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
}

fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: IntRange) {
    addStyle(style, range.first, range.last + 1)
}
