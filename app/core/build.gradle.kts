import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.configuration.qaProjectConfiguration

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.aboutlibraries")
    id("ru.vladislavsumin.convention.compose-stability-analyzer")
    id("com.codingfeline.buildkonfig")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.adb.client)

            implementation(projects.feature.debug.impl)
            implementation(projects.feature.deviceLogDump.impl)
            implementation(projects.feature.adbDevice.impl)
            implementation(projects.feature.adbDeviceList.impl)
            implementation(projects.feature.bottomBar.impl)
            implementation(projects.feature.homeScreen.impl)
            implementation(projects.feature.legalInfo.impl)
            implementation(projects.feature.mcp.impl)
            implementation(projects.feature.logViewer.impl)
            implementation(projects.feature.logRecent.impl)
            implementation(projects.feature.logParser.anime)
            implementation(projects.feature.memoryIndicator.impl)
            implementation(projects.feature.notifications.impl)
            implementation(projects.feature.rootScreen.impl)
            implementation(projects.feature.settings.impl)
            implementation(projects.feature.tabs.impl)
            implementation(projects.feature.windowTitle.impl)
            implementation(projects.feature.multiWindow.impl)

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

buildkonfig {
    packageName = "ru.vladislavsumin.qa"
    objectName = "BuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "version", project.projectConfiguration.version)
        buildConfigField(
            BOOLEAN,
            "composeStabilityAnalyzerEnabled",
            qaProjectConfiguration.composeStabilityAnalyzer.enabled.toString(),
        )
    }
}
