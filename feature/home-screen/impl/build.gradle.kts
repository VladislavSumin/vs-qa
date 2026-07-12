plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.multiWindow.api)
            implementation(projects.feature.notifications.api)
            implementation(projects.feature.adbDevice.api)
            implementation(projects.feature.adbDeviceList.api)
            implementation(projects.feature.legalInfo.api)
            implementation(projects.feature.logRecent.api)
            implementation(projects.feature.logViewer.api)
            implementation(projects.feature.settings.api)
            implementation(projects.feature.tabs.api)
        }
    }
}
