package ru.vladislavsumin.core.adb.client

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import java.io.FileOutputStream

/**
 * Соединение с ADB, позволяет выполнять команды.
 *
 * Создает **отдельное** tcp соединение на каждую команду (это требование adb).
 */
internal class AdbConnection(private val dispatchers: VsDispatchers, private val selector: SelectorManager) {
    suspend fun executeTransportCommand(transport: String, command: String): String = withContext(dispatchers.IO) {
        withConnection { r, w ->
            // Утсанавливаем транспорт
            w.sendAdbData(transport)
            r.checkAdbStatus()
            // Выполняем команду
            w.sendAdbData(command)
            r.checkAdbStatus()
            // Получаем ответ
            r.receiveShellOutput()
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

    fun executeContinuousTransportCommand(transport: String, command: String): Flow<String> = flow {
        withConnection { r, w ->
            w.sendAdbData(transport)
            r.checkAdbStatus()
            w.sendAdbData(command)
            r.checkAdbStatus()
            while (true) {
                val line = r.readUTF8Line() ?: break
                emit(line)
            }
        }
    }.flowOn(dispatchers.IO)

    suspend fun pullFile(transport: String, remotePath: String, localPath: String): Unit = withContext(dispatchers.IO) {
        withConnection { r, w ->
            w.sendAdbData(transport)
            r.checkAdbStatus()
            w.sendAdbData("sync:")
            r.checkAdbStatus()

            val pathBytes = remotePath.encodeToByteArray()
            w.writeFully("RECV".encodeToByteArray())
            w.writeIntLe(pathBytes.size)
            w.writeFully(pathBytes)
            w.flush()

            FileOutputStream(localPath).use { fos ->
                while (true) {
                    val header = r.readByteArray(8)
                    val command = header.copyOfRange(0, 4).decodeToString()
                    val len = header.copyOfRange(4, 8).readIntLe()

                    when (command) {
                        "DATA" -> {
                            fos.write(r.readByteArray(len))
                        }

                        "DONE" -> break

                        "FAIL" -> {
                            val msg = r.readByteArray(len).decodeToString()
                            error("ADB sync pull failed: $msg")
                        }

                        else -> error("Unknown sync response: $command")
                    }
                }
            }
        }
    }

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

    private suspend fun ByteReadChannel.receiveShellOutput(): String = buildString {
        while (true) {
            val line = readUTF8Line() ?: break
            append(line)
            append('\n')
        }
    }

    private suspend fun <T> withConnection(block: suspend (ByteReadChannel, ByteWriteChannel) -> T): T =
        aSocket(selector)
            .tcp()
            .connect(DEFAULT_HOST, DEFAULT_PORT)
            .use { socket ->
                val readChannel = socket.openReadChannel()
                val writeChannel = socket.openWriteChannel()
                block(readChannel, writeChannel)
            }

    companion object {
        private const val OKAY = "OKAY"
        private const val FAIL = "FAIL"
        private const val STATUS_LEN = 4
        private const val DATA_LEN_LEN = 4
        private const val DEFAULT_HOST = "127.0.0.1"
        private const val DEFAULT_PORT = 5037
    }

    @Suppress("MagicNumber")
    private fun ByteArray.readIntLe(): Int = (this[0].toInt() and 0xFF) or
        ((this[1].toInt() and 0xFF) shl 8) or
        ((this[2].toInt() and 0xFF) shl 16) or
        ((this[3].toInt() and 0xFF) shl 24)

    @Suppress("MagicNumber")
    private suspend fun ByteWriteChannel.writeIntLe(value: Int) {
        writeFully(
            byteArrayOf(
                (value and 0xFF).toByte(),
                ((value shr 8) and 0xFF).toByte(),
                ((value shr 16) and 0xFF).toByte(),
                ((value shr 24) and 0xFF).toByte(),
            ),
        )
    }
}
