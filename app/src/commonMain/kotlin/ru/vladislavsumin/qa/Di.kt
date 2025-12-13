package ru.vladislavsumin.qa

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import ru.vladislavsumin.core.adb.client.coreAdbClient
import ru.vladislavsumin.core.coroutines.dispatcher.coreCoroutinesDispatchers
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.fs.coreFs
import ru.vladislavsumin.core.navigation.coreNavigation
import ru.vladislavsumin.core.serialization.yaml.coreSerializationYaml
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.feature.logParser.anime.domain.featureAnimeLogParser
import ru.vladislavsumin.feature.logViewer.featureLogViewer
import ru.vladislavsumin.feature.windowTitle.featureWidowTitle
import ru.vladislavsumin.qa.feature.adbDeviceList.featureAdbDeviceList
import ru.vladislavsumin.qa.feature.bottomBar.featureBottomBar
import ru.vladislavsumin.qa.feature.homeScreen.featureHomeScreen
import ru.vladislavsumin.qa.feature.memoryIndicator.featureMemoryIndicator
import ru.vladislavsumin.qa.feature.notifications.featureNotifications
import ru.vladislavsumin.qa.feature.rootScreen.featureRootScreen

fun createDi(
    platformModule: DI.Module?,
    globalHotkeyManager: GlobalHotkeyManager,
): DirectDI = DI {
    if (platformModule != null) {
        importOnce(platformModule)
    }

    bindSingleton { globalHotkeyManager }

    importOnce(Modules.coreFs(appTechName = "vs-qa"))
    importOnce(Modules.coreSerializationYaml())
    importOnce(Modules.coreCoroutinesDispatchers())
    importOnce(Modules.coreAdbClient())
    importOnce(Modules.coreNavigation<ComponentContext>())

    importOnce(Modules.featureAdbDeviceList())
    importOnce(Modules.featureBottomBar())
    importOnce(Modules.featureHomeScreen())
    importOnce(Modules.featureLogViewer())
    importOnce(Modules.featureMemoryIndicator())
    importOnce(Modules.featureNotifications())
    importOnce(Modules.featureRootScreen())
    importOnce(Modules.featureWidowTitle())

    importOnce(Modules.featureAnimeLogParser())
}.direct
