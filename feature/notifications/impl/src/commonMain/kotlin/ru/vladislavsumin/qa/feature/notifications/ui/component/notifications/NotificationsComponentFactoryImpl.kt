package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import com.arkivanov.decompose.ComponentContext

internal class NotificationsComponentFactoryImpl(
    private val viewModelFactory: NotificationsViewModelFactory,
) : NotificationsComponentFactory {
    override fun create(context: ComponentContext): NotificationsComponent {
        return NotificationsComponentImpl(viewModelFactory, context)
    }
}
