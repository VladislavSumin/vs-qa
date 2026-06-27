plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.notifications.api)
            implementation(vsCoreLibs.vs.core.navigation.impl)
        }
    }
}
