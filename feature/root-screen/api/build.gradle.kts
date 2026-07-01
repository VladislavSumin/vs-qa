plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.navigation.impl)
            implementation(projects.feature.windowTitle.api)
        }
    }
}
