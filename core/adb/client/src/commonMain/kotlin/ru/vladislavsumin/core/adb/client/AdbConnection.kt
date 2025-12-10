package ru.vladislavsumin.core.adb.client

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.writeString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers

internal class AdbConnection(
    private val dispatchers: VsDispatchers,
    private val selector: SelectorManager,
) {
    suspend fun executeCommand(command: String): String = withContext(dispatchers.IO) {
        withConnection { r, w ->
            w.sendAdbData(command)
            r.checkAdbStatus()
            r.receiveAdbData()
        }
    }

    fun executeContinuousCommand(command: String): Flow<String> = flow {
        withConnection { r, w ->
            w.sendAdbData(command)
            r.checkAdbStatus()
            while (true) {
                emit(r.receiveAdbData())
            }
        }
    }.flowOn(dispatchers.IO)

    private suspend fun ByteWriteChannel.sendAdbData(data: String) {
        val len = data.length
        check(len <= UShort.MAX_VALUE.toInt()) { "Invalid data size len=$len" }
        val hexLen = len.toUShort().toHexString()
        writeString(hexLen)
        writeString(data)
        flush()
    }

    private suspend fun ByteReadChannel.checkAdbStatus() {
        when (val result = readByteArray(STATUS_LEN).decodeToString()) {
            OKAY -> Unit
            FAIL -> error("Operation fail")
            else -> error("Unknown response status $result")
        }
    }

    private suspend fun ByteReadChannel.receiveAdbData(): String {
        val len = readByteArray(DATA_LEN_LEN).decodeToString().hexToInt()
        return readByteArray(len).decodeToString()
    }

    private suspend fun <T> withConnection(block: suspend (ByteReadChannel, ByteWriteChannel) -> T): T {
        return aSocket(selector)
            .tcp()
            .connect(DEFAULT_HOST, DEFAULT_PORT)
            .use { socket ->
                val readChannel = socket.openReadChannel()
                val writeChannel = socket.openWriteChannel()
                block(readChannel, writeChannel)
            }
    }

    companion object {
        private const val OKAY = "OKAY"
        private const val FAIL = "FAIL"
        private const val STATUS_LEN = 4
        private const val DATA_LEN_LEN = 4
        private const val DEFAULT_HOST = "127.0.0.1"
        private const val DEFAULT_PORT = 5037
    }
}
