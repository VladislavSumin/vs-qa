package ru.vladislavsumin.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

@Composable
fun QaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val textColor = QaTheme.colorScheme.onSurface
    val borderColor = if (isError) QaTheme.colorScheme.logError else QaTheme.colorScheme.onSurfaceVariant
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = LocalTextStyle.current.copy(color = textColor),
        cursorBrush = SolidColor(textColor),
        decorationBox = { text ->
            Row(
                modifier
                    .border(border = BorderStroke(width = 1.dp, color = borderColor))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(Modifier.width(4.dp))
                }
                Box {
                    text()
                    if (placeholder != null && value.isEmpty()) {
                        val placeholderTextStyle =
                            LocalTextStyle.current.copy(color = QaTheme.colorScheme.onSurfaceVariant)
                        CompositionLocalProvider(LocalTextStyle provides placeholderTextStyle) {
                            placeholder()
                        }
                    }
                }
            }
        },
    )
}
