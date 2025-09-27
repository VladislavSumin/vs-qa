package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

interface NotificationsUiInteractor {
    suspend fun showNotification(notification: Notification)
}
