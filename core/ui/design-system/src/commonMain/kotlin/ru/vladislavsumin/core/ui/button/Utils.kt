package ru.vladislavsumin.core.ui.button

import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color

@Composable
internal fun IconToggleButtonColors.containerColor(enabled: Boolean, checked: Boolean): State<Color> {
    val target =
        when {
            !enabled -> disabledContainerColor
            !checked -> containerColor
            else -> checkedContainerColor
        }
    return rememberUpdatedState(target)
}

@Composable
internal fun IconToggleButtonColors.contentColor(enabled: Boolean, checked: Boolean): State<Color> {
    val target =
        when {
            !enabled -> disabledContentColor
            !checked -> contentColor
            else -> checkedContentColor
        }
    return rememberUpdatedState(target)
}
