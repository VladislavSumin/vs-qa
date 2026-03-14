package ru.vladislavsumin.core.adb.client

import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import java.util.Locale

interface AdbClient {
    fun observeDevices(): Flow<AdbResult<List<DeviceInfo>>>

    sealed interface AdbResult<T> {
        data class Ok<T>(val data: T) : AdbResult<T>
        data class Err<T>(val t: Throwable) : AdbResult<T>
    }

    data class DeviceInfo(
        val name: String,
        val status: ConnectionStatus,
    ) {
        enum class ConnectionStatus {
            Device,
            Authorizing,
            Offline,
            ;

            internal companion object {
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
    private val localAdbServerController = LocalAdbServerController()

    override fun observeDevices(): Flow<AdbClient.AdbResult<List<AdbClient.DeviceInfo>>> {
        return connection
            .executeContinuousCommand("host:track-devices")
            .map { data ->
                val result = data.lines()
                    .filter { it.isNotBlank() }
                    .map {
                        val (name, staus) = it.split("\t")
                        AdbClient.DeviceInfo(
                            name = name,
                            status = AdbClient.DeviceInfo.ConnectionStatus.fromString(staus),
                        )
                    }
                AdbClient.AdbResult.Ok(result) as AdbClient.AdbResult<List<AdbClient.DeviceInfo>>
            }
            .retry(retries = 3) {
                delay(RETRY_DELAY_MS)
                // TODO эмитить ошибку при ретрае.
                localAdbServerController.startAdbServer()
                true
            }
            .catch { emit(AdbClient.AdbResult.Err(it)) }
    }

    companion object {
        private const val RETRY_DELAY_MS = 100L
    }
}
