package ru.vladislavsumin.qa.feature.notifications

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsComponentFactory
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsComponentFactoryImpl
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsViewModelFactory

fun Modules.featureNotifications() = DI.Module("feature-notifications") {
    bindSingleton<NotificationsComponentFactory> {
        val vmf = NotificationsViewModelFactory()
        NotificationsComponentFactoryImpl(vmf)
    }
}
