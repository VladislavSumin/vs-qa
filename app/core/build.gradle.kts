plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.ui.hotkeyController)
            implementation(projects.core.adb.client)

            implementation(projects.feature.adbDevice.impl)
            implementation(projects.feature.adbDeviceList.impl)
            implementation(projects.feature.bottomBar.impl)
            implementation(projects.feature.homeScreen.impl)
            implementation(projects.feature.logViewer.impl)
            implementation(projects.feature.logRecent.impl)
            implementation(projects.feature.logParser.anime)
            implementation(projects.feature.memoryIndicator.impl)
            implementation(projects.feature.notifications.impl)
            implementation(projects.feature.rootScreen.impl)
            implementation(projects.feature.windowTitle.impl)

            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.logger.platform)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.fs.impl)
            implementation(vsCoreLibs.vs.core.coroutines.dispatcher)
            implementation(vsCoreLibs.vs.core.serialization.yaml)
            implementation(vsCoreLibs.vs.core.navigation.impl)
            implementation(vsCoreLibs.vs.core.navigation.di)

            implementation(vsCoreLibs.decompose.extensions.compose)
            implementation(vsCoreLibs.decompose.extensions.composeExperimental)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(vsCoreLibs.kotlin.coroutines.swing)
        }
    }
}
