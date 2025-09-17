plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.logParser.api)
        }
    }
}
