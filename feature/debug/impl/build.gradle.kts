plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.navigation.debug)
            implementation(projects.feature.tabs.api)
        }
    }
}
