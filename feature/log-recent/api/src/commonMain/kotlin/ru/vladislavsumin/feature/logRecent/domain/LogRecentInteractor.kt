package ru.vladislavsumin.feature.logRecent.domain

import java.nio.file.Path

interface LogRecentInteractor {
    /**
     * Создает новую или обновляет текущую запись о недавнем открытии того или иного лог файла.
     */
    suspend fun addOrUpdateRecent(path: Path)
}
