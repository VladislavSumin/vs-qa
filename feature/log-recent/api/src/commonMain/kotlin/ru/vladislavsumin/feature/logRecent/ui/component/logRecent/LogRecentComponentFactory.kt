package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import java.nio.file.Path

interface LogRecentComponentFactory {
    fun create(
        onOpenLogRecent: (path: Path) -> Unit,
        context: ComponentContext,
    ): ComposeComponent
}
