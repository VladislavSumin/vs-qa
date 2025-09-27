package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import ru.vladislavsumin.core.decompose.compose.ComposeComponent

interface NotificationsComponent : ComposeComponent {
    val notificationsUiInteractor: NotificationsUiInteractor
}
