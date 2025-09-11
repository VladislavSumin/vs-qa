plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.bottomBar.api)
            implementation(projects.feature.logViewer.api)
        }
    }
}
