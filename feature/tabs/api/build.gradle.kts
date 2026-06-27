plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.navigation.impl)
        }
    }
}
