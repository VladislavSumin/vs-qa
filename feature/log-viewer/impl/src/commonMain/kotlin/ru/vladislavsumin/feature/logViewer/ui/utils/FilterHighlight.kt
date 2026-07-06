package ru.vladislavsumin.feature.logViewer.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import ru.vladislavsumin.core.ui.designSystem.theme.QaColorScheme
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.component.filterBar.FilterRequestParser

@Composable
internal fun FilterRequestParser.RequestHighlight.colorize(): AnnotatedString {
    val colors = QaTheme.colorScheme
    return when (this) {
        is FilterRequestParser.RequestHighlight.InvalidSyntax -> buildAnnotatedString { append(raw) }

        is FilterRequestParser.RequestHighlight.Success -> buildAnnotatedString {
            append(raw)
            spans.forEach { span ->
                addStyle(span.category.toSpanStyle(colors), span.range)
            }
        }
    }
}

private fun FilterRequestParser.Category.toSpanStyle(colors: QaColorScheme): SpanStyle = when (this) {
    FilterRequestParser.Category.Field -> SpanStyle(color = colors.onSurfaceVariant)

    FilterRequestParser.Category.Operator -> SpanStyle(color = colors.onSurfaceVariant)

    FilterRequestParser.Category.Logic -> SpanStyle(color = colors.logWarn.primary)

    FilterRequestParser.Category.Bracket -> SpanStyle(color = colors.logDebug.primary)

    FilterRequestParser.Category.Escape -> SpanStyle(color = colors.logWarn.primary)

    FilterRequestParser.Category.SavedFilterRef ->
        SpanStyle(color = colors.logDebug.primary, fontStyle = FontStyle.Italic)

    FilterRequestParser.Category.Text -> SpanStyle(color = colors.onSurface)
}
