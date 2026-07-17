plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.adb.client)
            implementation(projects.feature.settings.api)
        }
    }
}
