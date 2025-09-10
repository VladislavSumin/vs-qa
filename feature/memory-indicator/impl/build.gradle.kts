plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(compose.material3)
        }
    }
}
