package ru.vladislavsumin.qa.feature.adbDevice

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenFactory
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.AdbDeviceScreenFactoryImpl
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.DeviceControlViewModelFactory
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.DeviceParameterInteractor
import ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice.DeviceParameterInteractorImpl

fun Modules.featureAdbDevice() = DI.Module("feature-adbDevice") {
    bindGenericNavigation { NavigationRegistrarImpl() }

    bindSingleton<DeviceParameterInteractor> { DeviceParameterInteractorImpl(i()) }

    bindSingleton<AdbDeviceScreenFactory> {
        val vmf = DeviceControlViewModelFactory(i())
        AdbDeviceScreenFactoryImpl(vmf)
    }
}
