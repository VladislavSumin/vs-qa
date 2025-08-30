package ru.vladislavsumin.qa.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import ru.vladislavsumin.qa.domain.logs.RawLogRecord

@Composable
fun RawLogRecord.colorize(): AnnotatedString {
    val levelColor = LevelColors.getLevelColor(raw.substring(level))
    return buildAnnotatedString {
        append(raw)
        addStyle(SpanStyle(color = MaterialTheme.colorScheme.onSecondary), time)
        addStyle(SpanStyle(color = levelColor), level)
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, range: IntRange) {
    addStyle(style, range.first, range.last + 1)
}
