package ru.vladislavsumin.feature.logParser.logcat.domain

/**
 * Собирает бинарный logcat entry (little-endian) для тестов.
 */
@Suppress("LongParameterList", "MagicNumber")
internal fun binaryLogcatEntry(
    pid: Int = 123,
    tid: Long = 456,
    sec: Long = 1_700_000_000,
    nsec: Long = 123_000_000,
    lid: Long = 0,
    uid: Long = 0,
    prio: Int = 4,
    tag: String = "Tag",
    message: String = "message",
    hdrSize: Int = 28,
): ByteArray {
    val payload = byteArrayOf(prio.toByte()) +
        tag.encodeToByteArray() + 0 +
        message.encodeToByteArray() + 0

    val buf = ByteArray(hdrSize + payload.size)
    writeU16(buf, 0, payload.size)
    writeU16(buf, 2, hdrSize)
    writeU32(buf, 4, pid.toLong())
    writeU32(buf, 8, tid)
    writeU32(buf, 12, sec)
    writeU32(buf, 16, nsec)
    if (hdrSize >= 24) writeU32(buf, 20, lid)
    if (hdrSize >= 28) writeU32(buf, 24, uid)
    payload.copyInto(buf, hdrSize)
    return buf
}

@Suppress("MagicNumber")
private fun writeU16(buf: ByteArray, offset: Int, value: Int) {
    buf[offset] = (value and 0xFF).toByte()
    buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
}

@Suppress("MagicNumber")
private fun writeU32(buf: ByteArray, offset: Int, value: Long) {
    buf[offset] = (value and 0xFF).toByte()
    buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
    buf[offset + 2] = ((value shr 16) and 0xFF).toByte()
    buf[offset + 3] = ((value shr 24) and 0xFF).toByte()
}
