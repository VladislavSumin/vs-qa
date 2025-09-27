package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import java.util.concurrent.atomic.AtomicInteger

@ConsistentCopyVisibility
data class Notification private constructor(
    val id: Int,
    val text: String,
    val servility: Servility,
) {
    constructor(
        text: String,
        servility: Servility,
    ) : this(ids.getAndIncrement(), text, servility)

    private companion object {
        private val ids = AtomicInteger()
    }

    enum class Servility {
        Error,
    }
}
