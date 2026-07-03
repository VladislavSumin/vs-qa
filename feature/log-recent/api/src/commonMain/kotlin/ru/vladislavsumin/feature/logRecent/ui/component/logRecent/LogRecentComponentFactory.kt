package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import java.nio.file.Path

interface LogRecentComponentFactory {
    fun create(
        notificationsUiInteractor: NotificationsUiInteractor,
        onOpenLogRecent: (path: Path, openInNewWindow: Boolean) -> Unit,
        context: ComponentContext,
    ): ComposeComponent
}
