package ru.vladislavsumin.qa.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import ru.vladislavsumin.qa.domain.logs.RawLogRecord
import ru.vladislavsumin.qa.ui.theme.QaTheme

@Composable
fun RawLogRecord.colorize(): AnnotatedString {
    val levelColor = LevelColors.getLevelColor(raw.substring(level))
    return buildAnnotatedString {
        append(raw)
        addStyle(SpanStyle(color = QaTheme.colorScheme.onSurfaceVariant), time)
        addStyle(SpanStyle(color = levelColor), level)
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: IntRange) {
    addStyle(style, range.first, range.last + 1)
}
