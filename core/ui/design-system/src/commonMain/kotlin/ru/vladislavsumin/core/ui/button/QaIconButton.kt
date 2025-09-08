package ru.vladislavsumin.core.ui.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun QaIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val colors = IconButtonDefaults.outlinedIconToggleButtonColors()
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Checkbox },
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        color = colors.containerColor(enabled, false).value,
        contentColor = colors.contentColor(enabled, false).value,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.size(24.dp),
        ) {
            Box(
                Modifier.matchParentSize().padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
