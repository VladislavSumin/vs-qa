plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.windowTitle.api)
            implementation(projects.feature.adbDevice.api)
            implementation(projects.feature.bottomBar.api)
            implementation(projects.feature.homeScreen.api)
            implementation(projects.feature.logViewer.api)
            implementation(projects.feature.logRecent.api)
            implementation(projects.feature.tabs.api)
            implementation(projects.feature.notifications.api)
            implementation(vsCoreLibs.vs.core.serialization.yaml)
        }
    }
}
