package ru.vladislavsumin.feature.logViewer.domain.logs

/**
 * Информация о конкретном запуске (ране).
 *
 * @param orderRange номера лог записей которые относятся к этому запуску, по полю [LogRecord.order]
 * @param meta любая дополнительная информация о ране
 */
data class RunIdInfo(val orderRange: LogOrderRange, val meta: Map<String, String>,)
