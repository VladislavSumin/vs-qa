package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import ru.vladislavsumin.core.ui.resources.ResourceString
import java.util.concurrent.atomic.AtomicInteger

@ConsistentCopyVisibility
data class Notification private constructor(val id: Int, val text: ResourceString, val servility: Servility) {
    constructor(
        text: ResourceString,
        servility: Servility,
    ) : this(ids.getAndIncrement(), text, servility)

    private companion object {
        private val ids = AtomicInteger()
    }

    enum class Servility {
        Error,
    }
}
