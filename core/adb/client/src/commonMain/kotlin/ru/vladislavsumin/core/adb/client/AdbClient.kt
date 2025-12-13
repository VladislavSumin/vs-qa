package ru.vladislavsumin.core.adb.client

import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import java.util.Locale

interface AdbClient {
    fun observeDevices(): Flow<List<DeviceInfo>>

    data class DeviceInfo(
        val name: String,
        val status: ConnectionStatus,
    ) {
        enum class ConnectionStatus {
            Device,
            Authorizing,
            Offline,
            ;

            companion object {
                fun fromString(raw: String): ConnectionStatus {
                    // TODO написать нормальную реализацию.
                    return valueOf(
                        raw.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                    )
                }
            }
        }
    }
}

internal class AdbClientImpl(
    dispatchers: VsDispatchers,
) : AdbClient {
    // TODO получать извне
    private val selector = SelectorManager(dispatchers.IO)

    private val connection = AdbConnection(dispatchers, selector)

    override fun observeDevices(): Flow<List<AdbClient.DeviceInfo>> {
        return connection.executeContinuousCommand("host:track-devices").map { data ->
            data.lines()
                .filter { it.isNotBlank() }
                .map {
                    val (name, staus) = it.split("\t")
                    AdbClient.DeviceInfo(
                        name = name,
                        status = AdbClient.DeviceInfo.ConnectionStatus.fromString(staus),
                    )
                }
        }
    }
}
