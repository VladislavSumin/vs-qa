plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.tabs.api)

            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.m3)
        }
    }
}
