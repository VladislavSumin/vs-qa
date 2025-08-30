package ru.vladislavsumin.qa.ui.utils

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import ru.vladislavsumin.qa.domain.logs.RawLogRecord

@Composable
fun RawLogRecord.colorize(): AnnotatedString {
    val tagColor = TagColors.getTagColor(raw.substring(tag))
    return buildAnnotatedString {
        append(raw)
        addStyle(SpanStyle(color = MaterialTheme.colors.secondary), time.start, time.last)
        addStyle(SpanStyle(background = tagColor), tag.start, tag.last)
    }
}
