package ru.vladislavsumin.core.adb.client

import io.ktor.network.selector.SelectorManager
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers

interface AdbClient

internal class AdbClientImpl(
    dispatchers: VsDispatchers,
) : AdbClient {
    // TODO получать извне
    private val selector = SelectorManager(dispatchers.IO)

    @Suppress("unused")
    private val connection = AdbConnection(dispatchers, selector)
}
