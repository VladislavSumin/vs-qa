package ru.vladislavsumin.qa.feature.settings.domain

import kotlinx.coroutines.flow.Flow

/**
 * Предоставляет доступ к пользовательским настройкам приложения.
 */
interface SettingsInteractor {
    /**
     * Текущий выбранный пользователем язык приложения.
     */
    val language: Flow<AppLanguage>

    /**
     * Настройка расположения для сохранения дампов логов с устройства.
     */
    val dumpPathOption: Flow<DumpPathOption>
}
