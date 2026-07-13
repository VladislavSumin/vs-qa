package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource

interface TabSupport {
    val tabState: StateFlow<TabState>

    data class TabState(
        val icon: ImageVector? = null,
        val name: String? = null,
        val nameRes: StringResource? = null,
        val windowName: String? = name,
        val windowNameRes: StringResource? = nameRes,
        val allowClose: Boolean = true,
        val allowDetach: Boolean = false,
    )
}
