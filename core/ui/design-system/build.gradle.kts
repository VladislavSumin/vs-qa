plugins {
    id("ru.vladislavsumin.convention.preset.core")
    id("ru.vladislavsumin.convention.compose")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            implementation(libs.backdrop)
            implementation(libs.shapes)
            implementation(vsCoreLibs.vs.core.serialization.yaml)
        }
    }
}
