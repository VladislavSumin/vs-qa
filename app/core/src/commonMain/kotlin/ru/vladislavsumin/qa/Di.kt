package ru.vladislavsumin.qa

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.direct
import ru.vladislavsumin.core.adb.client.coreAdbClient
import ru.vladislavsumin.core.coroutines.dispatcher.coreCoroutinesDispatchers
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.fs.coreFs
import ru.vladislavsumin.core.navigation.coreNavigation
import ru.vladislavsumin.core.serialization.yaml.coreSerializationYaml
import ru.vladislavsumin.feature.logParser.anime.domain.featureAnimeLogParser
import ru.vladislavsumin.feature.logRecent.featureLogRecent
import ru.vladislavsumin.feature.logViewer.featureLogViewer
import ru.vladislavsumin.feature.logsDashboard.featureLogsDashboard
import ru.vladislavsumin.feature.mcp.domain.featureMcp
import ru.vladislavsumin.qa.feature.adbDevice.featureAdbDevice
import ru.vladislavsumin.qa.feature.adbDeviceList.featureAdbDeviceList
import ru.vladislavsumin.qa.feature.bottomBar.featureBottomBar
import ru.vladislavsumin.qa.feature.debug.featureDebug
import ru.vladislavsumin.qa.feature.deviceLogDump.featureDeviceLogDump
import ru.vladislavsumin.qa.feature.homeScreen.featureHomeScreen
import ru.vladislavsumin.qa.feature.legalInfo.featureLegalInfo
import ru.vladislavsumin.qa.feature.memoryIndicator.featureMemoryIndicator
import ru.vladislavsumin.qa.feature.multiWindow.featureMultiWindow
import ru.vladislavsumin.qa.feature.notifications.featureNotifications
import ru.vladislavsumin.qa.feature.rootScreen.featureRootScreen
import ru.vladislavsumin.qa.feature.settings.featureSettings
import ru.vladislavsumin.qa.feature.tabs.featureTabs

fun createDi(platformModule: DI.Module?): DirectDI = DI {
    if (platformModule != null) {
        importOnce(platformModule)
    }

    importOnce(Modules.coreFs(appTechName = "vs-qa"))
    importOnce(Modules.coreSerializationYaml())
    importOnce(Modules.coreCoroutinesDispatchers())
    importOnce(Modules.coreAdbClient())
    importOnce(Modules.coreNavigation<ComponentContext>())

    importOnce(Modules.featureDebug())
    importOnce(Modules.featureAdbDevice())
    importOnce(Modules.featureAdbDeviceList())
    importOnce(Modules.featureBottomBar())
    importOnce(Modules.featureDeviceLogDump())
    importOnce(Modules.featureHomeScreen())
    importOnce(Modules.featureLegalInfo())
    importOnce(Modules.featureLogRecent())
    importOnce(Modules.featureLogsDashboard())
    importOnce(Modules.featureLogViewer())
    importOnce(Modules.featureMcp())
    importOnce(Modules.featureMemoryIndicator())
    importOnce(Modules.featureNotifications())
    importOnce(Modules.featureRootScreen())
    importOnce(Modules.featureSettings())
    importOnce(Modules.featureTabs())
    importOnce(Modules.featureMultiWindow())

    importOnce(Modules.featureAnimeLogParser())
}.direct
