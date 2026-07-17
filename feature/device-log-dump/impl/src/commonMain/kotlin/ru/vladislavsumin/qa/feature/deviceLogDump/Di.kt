package ru.vladislavsumin.qa.feature.deviceLogDump

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.qa.feature.deviceLogDump.domain.DeviceLogDumpInteractor
import ru.vladislavsumin.qa.feature.deviceLogDump.domain.DeviceLogDumpInteractorImpl

fun Modules.featureDeviceLogDump() = DI.Module("feature-deviceLogDump") {
    bindSingleton<DeviceLogDumpInteractor> { DeviceLogDumpInteractorImpl(i(), i(), i()) }
}
