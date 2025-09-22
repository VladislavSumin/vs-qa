package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

@Composable
internal fun HelpMenuContent() {
    Box(Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        val highlightStyle = SpanStyle(
            color = QaTheme.colorScheme.logInfo.primary,
        )
        val highlightStyle2 = SpanStyle(
            color = QaTheme.colorScheme.logDebug.primary,
        )

        fun AnnotatedString.Builder.appendHighlight(highlight: String) {
            withStyle(highlightStyle) { append(highlight) }
        }

        fun AnnotatedString.Builder.appendHighlight2(highlight: String) {
            withStyle(highlightStyle2) { append(highlight) }
        }

        fun AnnotatedString.Builder.appendRecord(key: String, description: String) {
            append("* ")
            appendHighlight(key)
            append(" - ")
            appendLine(description)
        }

        Text(
            text = buildAnnotatedString {
                appendLine("Example syntax:")
                appendHighlight("level")
                append("=")
                appendHighlight2("i ")

                appendHighlight("tag")
                append(":=")
                appendHighlight2("MyTag ")

                appendHighlight("thread")
                append("=")
                appendHighlight2("MyThread ")

                appendHighlight("message")
                append("=")
                appendHighlight2("\"some message with \\\" for example\"")

                appendLine()
                appendLine()

                appendLine("Available filter functions:")
                appendRecord("tag", "filter by log tag")
                appendRecord("level", "filter by log level or above")
                appendRecord("thread", "filter by thread name")
                appendRecord("message", "filter by log message")
                appendRecord("timeAfter", "filter by time STRING with string comparator")
                appendRecord("timeBefore", "filter by time STRING with string comparator")
                appendRecord("<empty>", "filter by whole original log record")
                appendLine()
                appendLine("Available comparator functions:")
                appendRecord("=", "filter by contains")
                appendRecord(":=", "filter by exactly (not applying to time*, level)")
                appendLine()
                append("All different filters merges with ")
                appendHighlight("&&")
                append(" condition, all same filter merges by ")
                appendHighlight("||")
                append(" condition.")
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
