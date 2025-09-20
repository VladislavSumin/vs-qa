plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.bottomBar.api)

            // TODO подумать над навигацией
            implementation(vsCoreLibs.vs.core.navigation.impl)
        }
    }
}
