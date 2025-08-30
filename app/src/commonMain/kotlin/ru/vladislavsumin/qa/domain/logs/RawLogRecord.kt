package ru.vladislavsumin.qa.domain.logs

/**
 * Сырое log запись.
 * @param line номер первой строки с которой начинается этот лог.
 * @param order номер лога (так как одна запись может занимать несколько строк, то этот параметер в общем случаее
 * не равен параметру [line].
 * @param raw сырая log запись не разбитая на подкомпоненты.
 */
data class RawLogRecord(
    val line: Int,
    val order: Int,
    val raw: String,
    val time: String,
    val level: String,
    val thread: String,
    val tag: String,
    val message: String,
)
