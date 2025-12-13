package ru.vladislavsumin.qa.feature.adbDeviceList.domain

/**
 * Проверка доступности adb функционала для текущей OS.
 */
interface AdbFeatureAvailabilityInteractor {
    val isAvailable: Boolean
}
