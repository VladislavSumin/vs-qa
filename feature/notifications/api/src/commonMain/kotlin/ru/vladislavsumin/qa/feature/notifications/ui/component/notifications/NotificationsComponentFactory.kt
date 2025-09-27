package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import com.arkivanov.decompose.ComponentContext

interface NotificationsComponentFactory {
    fun create(context: ComponentContext): NotificationsComponent
}
