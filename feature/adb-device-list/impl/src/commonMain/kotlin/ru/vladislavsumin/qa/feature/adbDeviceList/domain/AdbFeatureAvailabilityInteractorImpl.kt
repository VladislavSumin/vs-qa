package ru.vladislavsumin.qa.feature.adbDeviceList.domain

internal class AdbFeatureAvailabilityInteractorImpl : AdbFeatureAvailabilityInteractor {
    override val isAvailable: Boolean = checkIsAdbFeatureAvailable()
}

internal expect fun checkIsAdbFeatureAvailable(): Boolean
