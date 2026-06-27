package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.StateFlow

interface TabSupport {
    val tabState: StateFlow<TabState>

    data class TabState(
        val icon: ImageVector? = null,
        val name: String? = null,
        val windowName: String? = name,
        val allowClose: Boolean = true,
    )
}
