plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.bottomBar.api)
            implementation(projects.feature.notifications.api)
            implementation(projects.core.ui.hotkeyController)
            implementation(vsCoreLibs.vs.core.navigation.impl)
        }
    }
}
