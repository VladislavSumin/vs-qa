package ru.vladislavsumin.core.ui.debug

import java.lang.management.ManagementFactory

internal actual fun totalGcCollections(): Long = ManagementFactory.getGarbageCollectorMXBeans()
    .sumOf { it.collectionCount }
