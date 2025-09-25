package ru.vladislavsumin.feature.logParser.domain.runId

import ru.vladislavsumin.feature.logParser.domain.RawLogRecord

/**
 * Предоставляет дополнительную мета информацию по лог записям, номер запуска.
 * Подразумевается, что один файл логов может содержать записи из нескольких последовательных запусков приложения.
 * Тогда нам может быть полезно знать на какой запуск мы сейчас смотрим, а так же фильтровать логи по номеру запуска
 */
interface RunIdParser {
    /**
     * @return список индексов в массиве [logs] начиная с которого начинается новый запуск.
     * Первый индекс всегда должен быть 0. Если невозможно распознать номера запусков необходимо вернуть null.
     */
    suspend fun provideRunIdMeta(logs: List<RawLogRecord>): List<RawRunIdInfo>?
}
