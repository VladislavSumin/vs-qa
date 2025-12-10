package ru.vladislavsumin.core.adb.client

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i

fun Modules.coreAdbClient(): DI.Module = DI.Module("core-adb-client") {
    bindSingleton<AdbClient> { AdbClientImpl(i()) }
}
