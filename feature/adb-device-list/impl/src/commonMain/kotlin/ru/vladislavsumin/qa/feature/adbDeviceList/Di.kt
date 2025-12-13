package ru.vladislavsumin.qa.feature.adbDeviceList

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.qa.feature.adbDeviceList.domain.AdbFeatureAvailabilityInteractor
import ru.vladislavsumin.qa.feature.adbDeviceList.domain.AdbFeatureAvailabilityInteractorImpl
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListComponentFactory
import ru.vladislavsumin.qa.feature.adbDeviceList.ui.component.adbDeviceList.AdbDeviceListComponentFactoryImpl

fun Modules.featureAdbDeviceList() = DI.Module("feature-adbDeviceList") {
    bindSingleton<AdbFeatureAvailabilityInteractor> { AdbFeatureAvailabilityInteractorImpl() }
    bindSingleton<AdbDeviceListComponentFactory> { AdbDeviceListComponentFactoryImpl() }
}
