package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory
internal class NotificationsViewModel : ViewModel(), NotificationsUiInteractor {
    private val notifications = MutableStateFlow(emptyList<Notification>())

    val state = notifications.map { notifications ->
        NotificationsViewState(notifications)
    }.stateIn(NotificationsViewState(emptyList()))

    override suspend fun showNotification(notification: Notification) {
        notifications.update { it + notification }
    }

    fun onClickCloseNotification(notificationId: Int) {
        notifications.update { it.filter { it.id != notificationId } }
    }
}
