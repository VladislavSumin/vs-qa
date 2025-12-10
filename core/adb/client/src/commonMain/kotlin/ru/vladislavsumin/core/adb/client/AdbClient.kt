package ru.vladislavsumin.core.adb.client

import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

// Test main
// TODO remove
fun main() = runBlocking {
    with(Dispatchers.IO) {
        AdbClientImpl().test()
    }
}

interface AdbClient

class AdbClientImpl : AdbClient {
    // TODO получать извне
    private val selector = SelectorManager(Dispatchers.IO)

    suspend fun test() {
        val connection = AdbConnection(selector)

        val data = connection.executeCommand("host:devices")
        println(data)

        connection.executeContinuousCommand("host:track-devices")
            .collect { value -> println(value) }
    }
}
